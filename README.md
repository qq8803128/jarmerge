一个可以配置删除第三方jar库中的class文件的android gradle插件

前端时间做一个项目，对于网络请求有个需求，http状态码为200-299之前全部是success，由于使用了retrofit2，retrofit2中对于200-299之间的部分状态码有自己的处理，所以需要重写retrofit中的几个类，那么问题来了，如何完美的解决这个问题

1.找到本地仓库中的retrofit2.jar  删除我们要重新的类文件，然后在工程中自己重新这些类（这个方案可以说不怎么完美，主要是你删除了类文件，那么别的工程中也compile这个版本的retrofit2.jar的话 那些类都找不到了，因此不考虑）

2.将retrofit2.jar复制到工程中，再删除（缺点是如果每个工程都需要复制和删除，太麻烦）

考虑了一段时间，看了下gradle plugin的开发流程，找到了一个解决方案，利用transform来实现，对于android将jar打包进app的流程，发现了app是如何将jar导包到app中的，gradle从仓库、libs或者compile project中生成jar或者aar 到project的transform目录  这个时候我们可以对jar进行删除文件，这样怼原始jar无任何侵入操作

使用方法：工程的build.gradle中添加

    classpath 'org.ollyice.gradle:merge-gradle-plugin:1.1.0'
    
在app的build.gradle中添加

    apply plugin: 'org.ollyice.merge'
    
merge{   
  enabled true   
  
  log true
  
  priority('common')//优先级 
  
  //unique是如果多个jar中有相同的class  将优先保留priority中的相同的class文件  删除其他的jar中的class文件
  //delete是删除所有jar中的class文件
  unique('retrofit2.OkHttpCall**')
  
  unique('retrofit2.converter.gson.GsonResponseBodyConverter**') 
  
  unique('retrofit2.ServiceMethod**')
}
