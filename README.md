Sbt Application Plugin
===========
`Sbt Application Plugin`是`SBT`构建工具的一个插件，主要目标是为简单的应用打包。

#### 打包目录

        ├─bin/*   //bin 目录下的脚本
        ├─conf    //conf目录下的配置
        ├─lib     //所有依赖的库(jar)

#### 编译安装
+ 下载`git clone git@github.com:yafengli/sbt-application-plugin.git`

+ 发布到本地`Ivy2 Repository`

        cd sbt-dist-plugin
        sbt 
        > ^ publishLocal     // cross build for sbt@1.2.8+

+ 发布到本地`Maven Repository`

        sbt 
        > ^ publishM2
        
+ 特定仓库发布，修改`build.sbt`:`publishTo := Some(Resolver.file("file",  new File("path/your/m2repo")))`

        sbt 
        > ^ publish       
       
#### 使用
在使用`SBT`的项目中，修改`project/plugins.sbt`文件中内容：

        addSbtPlugin("org.koala" %% "sbt-dist" % "2.2.1")

在构建脚本`build.sbt`中添加内容:    

        lazy val name = project.in(file(".")).enablePlugins(SbtDistPlugin)
                                             .settings(mainClass := Some("[demo.class.name]"))
                                             .settings(dirSetting ++= Seq("conf","bin"))

#### 配置属性
+ `mainClass`：定义该配置会在`bin`生成缺省启动脚本，启动脚本缺省以`name`
+ `dirSetting`：需要额外打包的相对目录名`Seq[String]`

#### 构建发布
+ 打包：`sbt distZip`
+ 生成文件`target/universal/[orgination]-[name]-[version].zip`

#### 运行

        unzip [orgination]-[name]-[version].zip
        bin/[name].bat                          //Windows
        chmod 777 bin/[name] && bin/[name]      //Linux


#### Change Log

* 2.0.2 upgrade to sbt 1.2.x modify Linux & Window scripts.

* 2.0.1 upgrade to sbt 1.1.0. modify Linux & Window scripts.

* 2.0.0 everything is ok.