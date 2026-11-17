package org.greenhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import org.greenhouse.ui.navigation.ModulosRotas

class PrincipalViewModel : ViewModel() {
    val backStackModulos = NavBackStack<ModulosRotas>(ModulosRotas.categorias)

    fun abrirModulo(modulo: ModulosRotas) {
        backStackModulos.clear()
        backStackModulos.add(modulo)
    }
}
