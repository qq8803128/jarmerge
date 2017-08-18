package org.ollyice.merge;

class MergeExtension{
    boolean enabled;
    boolean log;
    /*
    path priority list
    when you compile the project
    this plugin will sort input jars
     */
    List<String> prioritys = new ArrayList<>();

    /*
    deletes files list
    when you comile the project
    this plugin will delete *.class in this list
     */
    List<String> deletes = new ArrayList<>();
    List<String> vagueDeletes = new ArrayList<>();

    /*
    unique class files list
    when you compile the project
    this plugin will retain the first class in unique list and delete others
    it can solve multidex class exception
     */
    List<String> uniques = new ArrayList<>();
    List<String> vagueUniques = new ArrayList<>();

    /**/
    List<String> tasks = new ArrayList<>();

    MergeExtension() {
        super()
    }

    def void delete(String path){
        if (path.endsWith("**")){
            if (!vagueDeletes.contains(path)){
                vagueDeletes.add(path)
            }
        }else{
            if (!deletes.contains(path)){
                deletes.add(path)
            }
        }
    }

    def void unique(String path){
        if (path.endsWith("**")){
            if (!vagueUniques.contains(path)){
                vagueUniques.add(path)
            }
        }else{
            if (!uniques.contains(path)){
                uniques.add(path)
            }
        }
    }

    def void priority(String path){
        if (!prioritys.contains(path)){
            prioritys.add(path)
        }
    }

    def void enabled(boolean enabled){
        this.enabled = enabled;
    }

    def void log(boolean enabled){
        log = enabled;
    }

    def void task(String t){
        if (!tasks.contains(t)){
            tasks.add(t)
        }
    }
}