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
        project.extensions.create(MERGE_EXTENSION, MergeExtension.class)
        if (project.plugins.hasPlugin(com.android.build.gradle.AppPlugin)) {
            print('registerTransform success\n')
            AppExtension android = project.extensions.getByType(AppExtension)
            android.registerTransform(new MergeTransform(project))
        }

    }
}