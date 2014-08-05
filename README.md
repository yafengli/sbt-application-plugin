Sbt Application Plugin
===========

#### 简介
`Sbt Application Plugin`是`SBT`构建工具的一个插件，主要目标是为简单的应用打包，项目目录类似：

        ├─bin                         //执行脚本
        ├─conf                        //应用配置
        ├─project                     //sbt配置
        ├─src                         //源文件目录
        │ ├─main
        │ │ ├─java
        │ │ ├─resources
        │ │ └─scala   
        │ └─test
        │     ├─java
        │     └─scala
        └─target

打包后的应用目录结构类似：

        ├─*       //bin目录下的执行脚本
        ├─conf    //conf目录下的配置
        ├─lib     //所有依赖的库(jar)

#### 安装
        cd sbt-application-plugin
        sbt publish-local


#### 使用
在project/plugins.sbt文件中添加内容：

        addSbtPlugin("org.koala" %% "sbt-application-plugin" % "1.0.0")
在.scala/.sbt中添加类似内容:    
        
        import org.koala.sbt.SbtAppPlugin._

    	lazy val projectName = project.in(file".")).settings(appSettings : _*).settings(prefix := "test",dirSetting ++= Seq("ext" -> "ext_dir")   )

#### 配置
需要配置两个参数：
+ `prefix`：打包文件名前缀，缺省为`organization-name-version`；
+ `dirSetting`：打包包含文件路径`Map`，`key`为包含的目录，`value`为打包文件的目录，缺省值`Map("conf" -> "conf", "bin" -> "","lib" -> "lib")`；

#### 命令

        sbt dist-zip
