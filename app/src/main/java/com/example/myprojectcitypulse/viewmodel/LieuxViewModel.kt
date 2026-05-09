
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.repository.LieuxRepository
import kotlinx.coroutines.launch

class LieuxViewModel(private val repository: LieuxRepository) : ViewModel() {

    //  garde une copie des lieux
    private var listeComplete: List<Lieux> = emptyList()

    val lieux = MutableLiveData<List<Lieux>>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun chargerLieux() {
        viewModelScope.launch {
            loading.value = true
            try {
                val data = repository.getLieux()
                listeComplete = data // stocke les donnees
                lieux.value = data
            } catch (e: Exception) {
                error.value = "Erreur de chargement"
            } finally {
                loading.value = false
            }
        }
    }

    // filtre  à partir de la liste complète
    fun filtreParCategorie(categorie: String) {
        if (categorie == "Tous") {
            lieux.value = listeComplete
        } else {
            lieux.value = listeComplete.filter {
                it.categorie.equals(categorie, ignoreCase = true)
            }
        }
    }

    //  recherche  à partir de la liste complète
    fun rechercher(text: String) {
        if (text.isEmpty()) {
            lieux.value = listeComplete
        } else {
            lieux.value = listeComplete.filter {
                it.nomlieu.contains(text, ignoreCase = true)
            }
        }
    }
}
