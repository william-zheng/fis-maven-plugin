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

现有问题
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

6、不支持 fis release --live

不知道怎么和浏览器通信

7、
现在只支持一个源码目录，如果你有多个module下都有源码需要fis处理，暂时不支持。

感谢
----
