package com.github.beachman4.fivemplugin.services

import com.github.beachman4.fivemplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
