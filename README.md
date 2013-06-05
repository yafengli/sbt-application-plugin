Sbt Application Plugin
===========

## 简介
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


## 安装
        cd sbt-application-plugin
        sbt publish-local


## 使用
在project/plugins.sbt文件中添加内容：

        addSbtPlugin("com.greatbit" %% "sbt-application-plugin" % "1.0.0")
在project/Build.scala中添加类似内容:
        
        import ApplicationPlugin._
    	lazy val akkatest = 
			Project(id = "test") settings(applicationSettings : _*) settings(
        		fileSetting := "test", 
        		dirSetting ++= Map("ext" -> "ext_dir")
    	)
`dirSetting`**缺省**`Map("conf" -> "conf", "bin" -> "","lib" -> "lib")`

## 配置
* `fileSetting` 打包文件名；
* `dirSetting`  打包包含文件路径Map，key为包含的目录，value为打包文件的目录；

## 命令
        sbt dist-zip
