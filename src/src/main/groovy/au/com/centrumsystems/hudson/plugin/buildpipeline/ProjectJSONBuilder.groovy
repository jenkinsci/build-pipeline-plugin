package au.com.centrumsystems.hudson.plugin.buildpipeline

import groovy.json.JsonBuilder

class ProjectJSONBuilder {

    static String asJSON(ProjectForm projectForm) {
        def entries = new ArrayList()
        projectForm.lastSuccessfulBuildParams?.each() { key, value ->
            entries.add({
                paramName(key)
                paramValue(value)
            })
        }

        def builder = new JsonBuilder()
        def root = builder {
            id(projectForm.id)
            name(projectForm.name)
            health(projectForm.health)
            url(projectForm.url)
            lastSuccessfulBuildNumber(projectForm.lastSuccessfulBuildNumber)
            lastSuccessfulBuildParams(entries.toArray())
        }
        return builder.toString()
    }
}