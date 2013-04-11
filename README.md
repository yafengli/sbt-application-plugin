Sbt Application Plugin
===========


## 安装
        git clone git@github.com:yafengli/sbt-application-plugin.git
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
* fileSetting 打包文件名；
* dirSetting  打包包含文件路径Map，key为包含的目录，value为打包文件的目录；

## 命令
        sbt dist-zip
