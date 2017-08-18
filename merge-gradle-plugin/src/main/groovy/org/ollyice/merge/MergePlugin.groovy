package org.ollyice.merge

import org.gradle.api.Plugin
import org.gradle.api.Project


class MergePlugin implements Plugin<Project>{
    static final String MERGE_EXTENSION = "merge";
    MergePlugin() {
        super()
    }

    @Override
    void apply(Project project) {
        print("MergePlugin loaded")
        project.extensions.create(MERGE_EXTENSION, MergeExtension.class)
        project.gradle.addListener(new MergeListener(project))
    }
}