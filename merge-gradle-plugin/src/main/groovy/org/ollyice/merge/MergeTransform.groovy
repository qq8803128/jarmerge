package org.ollyice.merge

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import net.lingala.zip4j.core.ZipFile
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class MergeTransform extends Transform {
    MergeExtension extension

    MergeTransform(Project project) {
        super()
        extension = project.extensions.getByName(MergePlugin.MERGE_EXTENSION)
    }

    @Override
    String getName() {
        return "mergeMultiClassJar"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (!extension || !extension.enabled) {
            super.transform(transformInvocation)
        } else {
            processTransform(transformInvocation)
        }
    }

    void processTransform(TransformInvocation transformInvocation) {
        List<String> jars = new ArrayList<>()
        Map<String,JarInput> jarmap = new HashMap<>()
        transformInvocation.inputs.each {
            it.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            it.jarInputs.each {JarInput jarInput->
                print(jarInput.file.absolutePath + '\n')
                jars.add(jarInput.file.absolutePath)
                jarmap.put(jarInput.file.absolutePath,jarInput)
            }
        }
        jars = sortJar(jars)
        List<String> outputJars = new ArrayList<>()
        jars.each {
            File jarInput = new File(it)
            String outputName = DigestUtils.md5Hex(jarInput.getAbsolutePath())
            File jarOutput = transformInvocation.outputProvider.getContentLocation(outputName,
                    jarmap.get(it).contentTypes, jarmap.get(it).scopes, Format.JAR)
            FileUtils.copyFile(jarInput, jarOutput)
            outputJars.add(jarOutput.absolutePath)
        }

        outputJars.each {
            ZipFile zipFile = new ZipFile(it)
            List<String> deletes = new ArrayList<>()
            zipFile.getFileHeaders().each {
                if (it.getFileName().endsWith('.class') || it.getFileName().endsWith('.CLASS')) {
                    String className = getClassName(it.getFileName())
                    if (needDelete(className)) {
                        deletes.add(it.getFileName())
                    }
                }
            }

            deletes.each {
                print 'delete ' + getClassName(it) + '\n'
                zipFile.removeFile(it)
            }
        }
    }

    String getClassName(String path) {
        return path.replaceAll('.class', '').replaceAll('/', '.')
    }

    boolean needDelete(String className) {
        boolean retn = false
        extension.vagueDeletes.each {
            String s = it.replaceAll('\\*\\*', '')
            if (className.startsWith(s)) {
                if (!extension.removes.contains(className)) {
                    extension.removes.add(className)
                }
            }
        }
        extension.deletes.each {
            String name = it
            if (name.equals(className)) {
                retn = true
                return
            }
            retn = false
            return
        }
        if (retn) return true

        extension.vagueUniques.each {
            String s = it.replaceAll('\\*\\*', '')
            if (className.startsWith(s)) {
                if (!extension.uniques.contains(className)) {
                    extension.uniques.add(className)
                }
            }
        }
        extension.uniques.each {
            String s = it
            if (s.equalsIgnoreCase(className)) {
                if (!extension.deletes.contains(s)) {
                    extension.deletes.add(s)
                } else {
                    retn = true
                }
            }
        }
        return retn
    }

    List<String> sortJar(List<String> jars){
        List<String> list = new ArrayList<>()
        List<String> temp = new ArrayList<>()
        extension.prioritys.each {
            String filter = it
            jars.each {
                if (it.contains(filter)){
                    list.add(it)
                }else{
                    temp.add(it)
                }
            }
        }
        list.addAll(temp)
        if (list.size() != jars.size()){
            list.addAll(jars)
        }
        return list
    }
}
