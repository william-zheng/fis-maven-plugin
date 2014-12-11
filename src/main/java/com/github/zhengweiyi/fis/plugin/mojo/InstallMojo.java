package com.github.zhengweiyi.fis.plugin.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * fis release 命令
 * @author Zhengweiyi
 *
 */
@Mojo(name="install")
public class InstallMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub

	}

}
