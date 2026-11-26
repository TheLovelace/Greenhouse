package org.greenhouse.ui.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeHeaderTest {
    @Test
    fun profileLabelsAreLocalizedForKnownRoles() {
        assertEquals("Funcionário", labelPerfilUsuario("FUNCIONARIO"))
        assertEquals("Administrador", labelPerfilUsuario("ADMIN"))
    }

    @Test
    fun unknownProfileIsPreserved() {
        assertEquals("OUTRO", labelPerfilUsuario("OUTRO"))
    }
}
