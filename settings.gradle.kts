import groovy.lang.MissingPropertyException

rootProject.name = extra["project.name"]?.toString() ?: throw MissingPropertyException("The project name was not configured!")
