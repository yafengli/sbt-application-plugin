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

        ├─bin/*   //bin目录下的执行脚本
        ├─conf    //conf目录下的配置
        ├─lib     //所有依赖的库(jar)

#### 安装
+ 发布到本地`Ivy2 Repository`
        
        sbt publishLocal

+ 发布到本地`Maven Repository`，修改`build.sbt`:`publishTo := Some(Resolver.file("file",  new File("path/your/m2repo")))`

        sbt publish

#### 使用
在project/plugins.sbt文件中添加内容：

        addSbtPlugin("org.koala" %% "sbt-application-plugin" % "1.1.1")
        
在.scala/.sbt中添加类似内容:    
        
        lazy val name = project.in(file".")).enablesPlugin(SbtDistApp).settings(mainClass := Some("demo.Hello"))

#### 配置属性
+ `mainClass`：定义该配置会在`bin`生成缺省启动脚本，启动脚本缺省以`name`
+ `dirSetting`：打包目录序列`Seq[String]`，缺省值`Seq("bin", "conf","lib")`，额外增加使用`.settings(dirSetting ++= Seq("ext"))`配置

#### 构建发布
+ 打包：`sbt distZip`
+ 生成文件`target/universal/[orgination]-[name]-[version].zip`

#### 运行

        unzip [orgination]-[name]-[version].zip
        bin/[name].bat                          //Windows
        chmod 777 bin/[name] && bin/[name]      //Linux