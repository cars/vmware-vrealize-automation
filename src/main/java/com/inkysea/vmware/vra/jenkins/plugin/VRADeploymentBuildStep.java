/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor. 
 */
package com.inkysea.vmware.vra.jenkins.plugin;

import com.inkysea.vmware.vra.jenkins.plugin.model.Deployment;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;
import com.inkysea.vmware.vra.jenkins.plugin.model.RequestParam;
import com.inkysea.vmware.vra.jenkins.plugin.util.EnvVariableResolver;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;



public class VRADeploymentBuildStep  extends Builder {

	private static final Logger LOGGER = Logger.getLogger(VRADeploymentBuildStep.class.getName());

	protected List<PluginParam> params;
	protected List<Deployment> deployments = new ArrayList<Deployment>();
	private List<RequestParam> requestParams;

	@DataBoundConstructor
	public VRADeploymentBuildStep(List<PluginParam> params) {
		this.params = params;
	}

	public List<PluginParam> getParams() {
		return params;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		LOGGER.finest("prebuild");
		return super.prebuild(build, listener);
	}


	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		LOGGER.finest("getProjectAction");
		return super.getProjectAction(project);
	}

	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		LOGGER.finest("getProjectActions");
		return super.getProjectActions(project);
	}


	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
							throws InterruptedException, IOException {
		LOGGER.entering(this.getClass().getSimpleName(),"perform()");
		EnvVars env = build.getEnvironment(listener);
		env.overrideAll(build.getBuildVariables());

		EnvVariableResolver helper = new EnvVariableResolver(build, listener);

		boolean success = true;

		int counter = 1;
		for (PluginParam param : params) {


				// Resolve any build variables included in the request paramaters.
			List<RequestParam> rparamResolved = new ArrayList<RequestParam>();;

				if ( ! (null == param.getRequestParams()) || param.getRequestParams().isEmpty()) {


					for (RequestParam rparam : param.getRequestParams()) {
						String rparamString = helper.replaceBuildParamWithValue(rparam.getRequestParam().toString());
						rparamResolved.add(new RequestParam(rparamString));
					}
				}

			// Resolve any environment variables in the parameters 
			PluginParam fparam = new PluginParam(helper.replaceBuildParamWithValue(param.getServerUrl()),
					helper.replaceBuildParamWithValue(param.getUserName()),
					helper.replaceBuildParamWithValue(param.getPassword()),
					helper.replaceBuildParamWithValue(param.getTenant()),
					helper.replaceBuildParamWithValue(param.getBlueprintName()),
					param.isWaitExec(),param.getRequestTemplate(), rparamResolved);

			final Deployment deployment = newDeployment(listener.getLogger(), fparam);


			if (deployment.create()) {
				this.deployments.add(deployment);
				//change counter to string and append bs for build step
				String strCounter = "BS_"+Integer.toString(counter);

				env.putAll(deployment.getDeploymentComponents(strCounter));

				build.addAction(new PublishEnvVarAction(deployment.getDeploymentComponents(strCounter)));

				counter++;
			} else {
				build.setResult(Result.FAILURE);
				success = false;
				break;
			}

		}
		LOGGER.exiting(this.getClass().getSimpleName(),"perform()");
		return success;

	}


	protected Deployment newDeployment(PrintStream logger, PluginParam params) throws IOException {
		LOGGER.entering(this.getClass().getSimpleName(),"newDeployment()");
		Boolean isURL = false;
		String recipe = null;
		LOGGER.exiting(this.getClass().getSimpleName(),"newDeployment()");
		return new Deployment(logger, params);

	}

	@Override
	public BuildStepDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final VRADeploymentBuildStep.DescriptorImpl DESCRIPTOR = new VRADeploymentBuildStep.DescriptorImpl();

	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public String getDisplayName() {
                    
			return "vRealize Automation Deployment";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
                
	}

	public class PublishEnvVarAction extends InvisibleAction implements EnvironmentContributingAction {
	// Required class to write variables back to Jenkins when part of a build step

		private Map<String, String> variables;

		public PublishEnvVarAction(Map<String, String> deploymentComponents) {
			this.variables = deploymentComponents;

		}


		public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
			env.putAll(variables);
		}

	}

}
