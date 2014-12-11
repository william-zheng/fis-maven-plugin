fis-maven-plugin
================

百度 fis 框架的 maven plugin

提供在maven下使用fis一些功能的插件，是基于fis的。

主要是解决一些在maven下使用fis时遇到的一些路径映射问题，似的在maven构建项目时可以像fis那样处理前端代码。

使用前必读
----
fis-maven-plugin毕竟和fis本身不同，所有有些fis的功能是不支持的。因为内容比较多，所以放在下面了。

这里可以先告诉你哪些支持，md5戳，pack打包，optimize压缩，domains添加域名，指定输出文件夹。如果你只用这些，那么继续读下去吧。

如何使用
----
如果你已经有了一个maven工程，那么你只需要改动一下pom.xml文件，增加一个.nodejs目录和一个package.json文件，就可以搞定啦，如果你已经下载了源码，那么找到fis-maven-plugin\src\it\fis-quickstart-demo，在目录下执行

    mvn tomcat7:run

然后访问

    http://localhost:8080/

是不是熟悉的fis-quickstart-demo？

如何配置
----
fis-maven-plugin 的配置

    <-- 1 -->
    <-- 2 -->
    <plugin>
        <groupId>com.github.zhengweiyi</groupId>
        <artifactId>fis-maven-plugin</artifactId>
        <executions>
            <execution>
                <id>fis release</id>
                <goals>
                    <goal>release</goal>
                </goals>
                <phase>generate-resources</phase>
                <configuration>
                    <md5>true</md5>
                    <optimize>true</optimize>
                </configuration>
            </execution>
        </executions>
    </plugin>

fis-maven-plugin无法单独工作，需要配合frontend-maven-plugin使用，原因是fis-maven-plugin只负责调用fis处理资源。fis的下载安装都不处理，有专门的frontend-maven-plugin插件负责nodejs的安装、npm的安装、fis的安装。其实frontend-maven-plugin的作用不止这些，有兴趣的朋友可以移驾这里，查看详细。

所以请在上面<-- 2 -->的位置配置如下代码

    <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>0.0.19</version>
        <inherited>false</inherited>
        <executions>
            <execution>
                <id>install node and npm</id>
                <goals>
                    <goal>install-node-and-npm</goal>
                    <goal>npm</goal>
                </goals>
                <phase>generate-resources</phase>
                <configuration>
                    <nodeVersion>v0.10.33</nodeVersion>
                    <npmVersion>1.4.28</npmVersion>
                    <nodeDownloadRoot>http://npm.taobao.org/dist/</nodeDownloadRoot>
                    <npmDownloadRoot>http://registry.npm.taobao.org/npm/-/</npmDownloadRoot>
                    <workingDirectory>${basedir}/.nodejs</workingDirectory>
                </configuration>
            </execution>
        </executions>
    </plugin>

看到了吧，我们把nodejs安装到了${basedir}/.nodejs路径下，并且还使用了淘宝的nodejs镜像。对我来说，的确快很多。

还有，因为fis处理了源码，所有需要告诉tomcat插件源码的位置变了。请在<-- 1 -->的地方配置如下代码

    <plugin>
        <groupId>org.apache.tomcat.maven</groupId>
        <artifactId>tomcat7-maven-plugin</artifactId>
        <version>2.2</version>
        <configuration>
            <path>/</path>
            <warSourceDirectory>${project.build.directory}/fis</warSourceDirectory>
        </configuration>
    </plugin>

当然，仅有上面3段代码还是不能工作的，我把完整的build段贴出来。

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.tomcat.maven</groupId>
                    <artifactId>tomcat7-maven-plugin</artifactId>
                    <version>2.2</version>
                    <configuration>
                        <path>/</path>
                        <warSourceDirectory>${project.build.directory}/fis</warSourceDirectory>
                    </configuration>
                </plugin>
                
                <plugin>
                    <groupId>com.github.zhengweiyi</groupId>
                    <artifactId>fis-maven-plugin</artifactId>
                    <version>0.1.0</version>
                </plugin>
            </plugins>
        </pluginManagement>

        <plugins>
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>0.0.19</version>
                <inherited>false</inherited>
                <executions>
                    <execution>
                        <id>install node and npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <nodeVersion>v0.10.33</nodeVersion>
                            <npmVersion>1.4.28</npmVersion>
                            <nodeDownloadRoot>http://npm.taobao.org/dist/</nodeDownloadRoot>
                            <npmDownloadRoot>http://registry.npm.taobao.org/npm/-/</npmDownloadRoot>
                            <workingDirectory>${basedir}/.nodejs</workingDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>com.github.zhengweiyi</groupId>
                <artifactId>fis-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>fis release</id>
                        <goals>
                            <goal>release</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <md5>true</md5>
                            <optimize>true</optimize>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

路径对应关系
----

* nodejs 安装目录

    fis-maven-plugin采用的是非全局的安装方式（因为frontend-maven-plugin就是这么做的，好像可以避免多版本冲突吧。）
    
    默认是项目的根目录下的.nodejs文件夹，应该是和跟pom.xml同级目录
    
    默认值${basedir}/.nodejs 可以使用nodejs_base参数更改

* 需要处理的源码目录

    默认是 ${basedir}/src/main/webapp 可以使用webSrcBase参数更改

* 源码处理完毕后存放目录

    默认是 ${project.build.directory}/fis 即target/fis 可以使用destPath更改。
    
    注意，需要将tomcat等插件读源码的位置改为destPath指定的路径，因为tomcat插件读源码的位置是 ${basedir}/src/main/webapp。

安装插件
----
原谅我没有把插件提交到公共仓库，我实在不知道怎么提交.....

现在只支持源码安装

下载源码，地址https://github.com/william-zheng/fis-maven-plugin

如果使用命令

    mvn install
    
你可以上传到公司的私有仓库里，这样小伙伴们就能直接使用了。

特别说明
----

不支持fis 特性如下：

1. 没有 fis install 命令

    我暂时不知道那个是干什么的，也没用到

2. 没有 fis server 命令

    maven下的web server太多了，而且maven本身就是为了java开发的，好像不用支持php

3. 不支持 fis release --lint 

    才疏学浅，不知道干什么用的，但是可以配置，请诸君使用告诉我有什么输出或返回值

4. 不支持 fis release --test

    同上

5. 部分支持 fis release --dest

    fis的dest命令后可以跟路径或名称，本插件是支持路径的（使用destPath），名称暂不支持（对fis的实现细节不清楚）

6. 不支持 fis release --live

    不知道怎么和浏览器通信

7. 现在只支持一个源码目录，如果你有多个module下都有源码需要fis处理，暂时不支持。

8. .nodejs/package.json

    这个文件是给npm计算依赖用的，如果你不喜欢，可以删除，然后在frontend-maven-plugin的参数中添加
    
        <arguments>install fis</arguments>
        
    也可以达到一样的效果
    
参数
----

* md5

    类型:boolean  默认值:false  含义:是否在编译的时候可以对文件自动加md5戳
   
* lint

    类型:boolean  默认值:false  含义：是否在编译的时候根据项目配置自动代码检查

* test
	
	类型:boolean  默认值:false  含义：是否在编译的时候对代码进行自动化测试
	
* pack
	
	类型:boolean  默认值:false  含义：是否对产出文件根据项目配置进行打包

* optimize

    类型:boolean  默认值:false  含义：是否对js、css、html进行压缩

* domains

    类型:boolean  默认值:false  含义：是否为资源添加domain域名
	
* destPath

    类型:String 默认值${project.build.directory}/fis 即 --dest Path 的形式
    
    注意和tomcat的路径保持一致
	
* destName

    暂不支持,写了也没用
	
* watch

    类型:boolean  默认值:false  含义：是否对项目进行增量编译，监听文件变化再触发编译
    
    特别提醒，使用了watch，maven就不会继续进行下面的处理，而是停留在监听状态，所以需要启动的同学最好使用命令 
    
        mvn fis:release -Dwatch=true
        
    单独启动一个处理进程。
	
* live

    暂不支持,写了也没用
	
* nodejs_base

    类型:String 默认值:${basedir}/.nodejs 
   
    命令行使用时参数名：extNodejsBase，nodejs安装的目录，请和frontend-maven-plugin插件的配置保持一致
	
* webSrcBase
	
	 类型:String 默认值:${basedir}/src/main/webapp，需要fis处理的资源文件的根目录

感谢
----
非常感谢 Eirik Sletteberg 的 frontend-maven-plugin 这个项目，其实之前完全不知道怎么写maven插件，我代码里好多也是照着葫芦画瓢得来的，虽然估计他看不懂中文，还是要感谢他。
