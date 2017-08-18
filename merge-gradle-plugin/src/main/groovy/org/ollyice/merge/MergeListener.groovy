package org.ollyice.merge

import net.lingala.zip4j.core.ZipFile
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Created by Administrator on 2017/8/17.
 */

public class MergeListener implements TaskExecutionListener, BuildListener {
    Logger logger
    Project mProject
    MergeExtension extension
    final String DEFAULT_TASK_NAME = "transformClassesWithDexFor"
    final String DEFAULT_TASK_NAME1 = "transformClassesWithJarMergingFor"
    Map<String, String> storeJars = new HashMap<>()
    List<String> jars = new ArrayList<>()

    public MergeListener(Project project) {
        super()
        mProject = project
        logger = LoggerFactory.getLogger('merge-log')
    }

    @Override
    public void buildStarted(Gradle gradle) {

    }

    @Override
    public void settingsEvaluated(Settings settings) {

    }

    @Override
    public void projectsLoaded(Gradle gradle) {

    }

    @Override
    public void projectsEvaluated(Gradle gradle) {

    }

    @Override
    public void buildFinished(BuildResult buildResult) {
        if (extension && extension.log && extension.enabled) {
            for (String name : storeJars.keySet()) {
                resetJar(name, storeJars.get(name))
            }
        }
    }

    @Override
    public void beforeExecute(Task task) {
        //get MergeExtension
        extension = (MergeExtension) mProject.getExtensions().getByName(MergePlugin.MERGE_EXTENSION);
        if (task.name.contains(DEFAULT_TASK_NAME) || task.name.contains(DEFAULT_TASK_NAME1) || isCustomTask(task.name)) {
            if (extension && extension.enabled && task.inputs.files.files) {
                List<String> files = new ArrayList<>()

                task.inputs.files.files.each {
                    if (isProjectJar(it.absolutePath)) {
                        files.add(it.absolutePath)
                    }
                }

                //sort files
                files = sort(files)

                //process jar and class
                files.each {
                    logger.error('file:' + it + '\n')
                    processJar(it)
                }
            }
        }

    }

    boolean isCustomTask(String task) {
        boolean retn = false
        if (extension) {
            extension.tasks.each {
                if (task.contains(it)) {
                    retn = true
                    return
                }
            }
        }
        return retn
    }

    boolean isProjectJar(String path) {
        if (path.endsWith(".jar") || path.endsWith(".JAR")) {
            if (!path.endsWith("android.jar")) {
                return true
            }
        }
        return false
    }

    @Override
    public void afterExecute(Task task, TaskState taskState) {

    }

    List<String> sort(List<String> list) {
        List<String> retn = new ArrayList<>()
        List<String> temp = new ArrayList<>()
        extension.prioritys.each {
            String priorityName = it;
            list.each {
                if (it.contains(priorityName)) {
                    retn.add(it)
                } else {
                    temp.add(it)
                }
            }
        }
        retn.addAll(temp)
        return retn
    }

    void processJar(String jar) {
        if (!jars.contains(jar)) {
            jars.add(jar)
            ZipFile zipFile = new ZipFile(jar)
            List<String> deletes = new ArrayList<>()
            zipFile.getFileHeaders().each {
                if (it.getFileName().endsWith('.class') || it.getFileName().endsWith('.CLASS')) {
                    String className = getClassName(it.getFileName())
                    if (needDelete(className)) {
                        deletes.add(it.getFileName())
                    }
                }
            }

            if (deletes.size() > 0) {
                if (extension.log){
                    logger.warn('delete class from file:' + jar + '\n')
                }
                delete(jar, zipFile, deletes)
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

    void delete(String jar, ZipFile zipFile, List<String> files) {
        String oldJarName = jar;
        String tmpJarName = jar + ".bak";
        if (storeJar(oldJarName, tmpJarName)) {
            storeJars.put(oldJarName, tmpJarName)
            files.each {
                zipFile.removeFile(it)
            }
        } else {
            if (extension.log) {
                logger.error("保存jar失败\n")
            }
        }
    }

    boolean storeJar(String s1, String s2) {
        try {
            File f1 = new File(s1)
            File f2 = new File(s2)
            if (f2.exists() && f2.isFile()) {
                f2.delete()
            }
            int length = 2097152;
            FileInputStream fis = new FileInputStream(f1);
            FileOutputStream out = new FileOutputStream(f2);
            byte[] buffer = new byte[length];
            while (true) {
                int ins = fis.read(buffer);
                if (ins == -1) {
                    fis.close();
                    out.flush();
                    out.close();
                    return true;
                } else {
                    out.write(buffer, 0, ins);
                }
            }
        } catch (Exception e) {
            return false
        }
        return true
    }

    void resetJar(String s1, String s2) {
        File f1 = new File(s1);
        if (f1.exists() && f1.isFile()) {
            f1.delete();
        }
        File f2 = new File(s2);
        f2.renameTo(f1);
    }
}
