package com.github.beachman4.fivem.services

import com.github.beachman4.fivem.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
