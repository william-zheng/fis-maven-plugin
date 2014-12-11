package com.github.zhengweiyi.fis.plugin.mojo;

import static com.github.zhengweiyi.fis.plugin.util.Utils.normalize;
import static com.github.zhengweiyi.fis.plugin.util.Utils.prepend;
import static com.github.zhengweiyi.fis.plugin.util.Utils.implode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.LoggerFactory;

import com.github.zhengweiyi.fis.plugin.util.NodeExecutor;
import com.github.zhengweiyi.fis.plugin.util.Platform;
import com.github.zhengweiyi.fis.plugin.util.ProcessExecutionException;
import com.github.zhengweiyi.fis.plugin.util.TaskRunnerException;

/**
 * fis release 命令
 * @author Zhengweiyi
 *
 */
@Mojo(name = "release", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ReleaseMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}")
	private MavenProject project;
	
	/**
	 * 添加 --md5 [level] 或 -m [level] 参数，在编译的时候可以对文件自动加md5戳，从此告别在静态资源url后面写?version=xxx的时代
	 */
	@Parameter(property = "md5", defaultValue = "false")
	private boolean md5;
	
	/**
	 * 添加 --lint 或 -l 参数，支持在编译的时候根据项目配置自动代码检查
	 * 暂不支持
	 */
	@Parameter(property = "lint", defaultValue = "false")
	private boolean lint;
	
	/**
	 * 添加 --test 或 -t 参数，支持在编译的时候对代码进行自动化测试
	 * 暂不支持
	 */
	@Parameter(property = "test", defaultValue = "false")
	private boolean test;
	
	/**
	 * 添加 --pack 或 -p 参数，对产出文件根据项目配置进行打包
	 */
	@Parameter(property = "pack", defaultValue = "false")
	private boolean pack;
	
	/**
	 * 添加 --optimize 或 -o 参数，对js、css、html进行压缩
	 */
	@Parameter(property = "optimize", defaultValue = "false")
	private boolean optimize;
	
	/**
	 * 添加 --domains 或 -D 参数，为资源添加domain域名
	 */
	@Parameter(property = "domains", defaultValue = "false")
	private boolean domains;
	
	/**
	 * 只支持输出到指定目录文件夹, 只支持绝对路径，或使用maven变量的相对路径
	 */
	@Parameter(property = "destPath", defaultValue = "${project.build.directory}/fis")
	private File destPath;
	
	/**
	 * 暂不支持
	 */
	@Parameter(property = "destName", defaultValue = "preview")
	private File destName;
	
	/**
	 * 添加 --watch 或 -w 参数，支持对项目进行增量编译，监听文件变化再触发编译
	 */
	@Parameter(property = "watch", defaultValue = "false")
	private boolean watch;
	
	/**
	 * 添加 --live 或 -L 参数，支持编译后自动刷新浏览器。Liveload功能需要浏览器支持Web Socket功能，例如Chrome、Firefox、Safari等浏览器。
	 * 暂不支持
	 */
	@Parameter(property = "live", defaultValue = "false")
	private boolean live;
	
	@Parameter(property = "extNodejsBase", defaultValue = "${basedir}/.nodejs")
	private File nodejs_base;
	
	@Parameter(property = "webSrcBase", defaultValue = "${basedir}/src/main/webapp")
	private File webSrcBase;
	
	private static File global_nodejs_base;

    private static final String TASK_NAME = "fis";
    private static final String TASK_LOCATION = "/node_modules/fis/bin/fis";
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if ( isRoot() ) {
			if (!nodejs_base.exists() || !nodejs_base.isDirectory()) {
				getLog().info( "nodejs workspace dir not find." );
				throw new MojoExecutionException( "nodejs workspace dir not find." );
			}
			global_nodejs_base = nodejs_base;
		}
		
		// ensure project is a web application
        if ( !isWar() )
        {
            getLog().info( "Skipping non-war project" );
            return;
        }
        
		Platform platform = Platform.guess();
		final String absoluteTaskLocation = global_nodejs_base.getPath() + normalize(TASK_LOCATION);
        final List<String> arguments = getArguments();
        getLog().info("Running " + taskToString(TASK_NAME, arguments) + " in " + webSrcBase);
        
        try {
			try {
			    final int result = new NodeExecutor(global_nodejs_base, prepend(absoluteTaskLocation, arguments), platform, webSrcBase).executeAndRedirectOutput(LoggerFactory.getLogger(getClass()));
			    if(result != 0){
			        throw new TaskRunnerException(taskToString(TASK_NAME, arguments) + " failed. (error code "+result+")");
			    }
			} catch (ProcessExecutionException e) {
			    throw new TaskRunnerException(taskToString(TASK_NAME, arguments) + " failed.", e);
			}
		} catch (TaskRunnerException e) {
			throw new MojoFailureException("Failed to run task", e);
		}
	}
	
	private boolean isWar() {
		return "war".equals(project.getPackaging());
	}
	
	private boolean isRoot() {
		return project.getParent() == null || project.getArtifactId().equals(project.getProperties().get("project.root"));
	}
	
	private List<String> getArguments() {
		List<String> arguments =  new ArrayList<String>();
		
		arguments.add("release");
		
		if (md5) {
			arguments.add("--md5");
		}
        if (lint) {
            arguments.add("--lint");
        }
        if (test) {
            arguments.add("--test");
        }
		if (pack) {
			arguments.add("--pack");
		}
		if (optimize) {
			arguments.add("--optimize");
		}
		if (domains) {
			arguments.add("--domains");
		}
        if (watch) {
            arguments.add("--watch");
        }
		if (destPath != null) {
			arguments.add("--dest");
			arguments.add("\"" + destPath.getPath() + "\"");
		}
		
		arguments.add("--no-color");
		
		return arguments;
	}

    protected static String taskToString(String taskName, List<String> commands) {
        return "'" + taskName + " " + implode(" ",commands) + "'";
    }
	

}
