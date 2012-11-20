package org.jboss.forge.plugin.gitignore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

@Alias("git-ignore")
@RequiresProject
@RequiresFacet(GitIgnoreFacet.class)
public class GitIgnorePlugin implements Plugin
{
   @Inject
   private Configuration config;
   
   @Inject
   private Shell shell;
   
   @Inject
   private ShellPrompt prompt;
   
   @Inject
   private ResourceFactory factory;
   
   @Inject
   private Event<InstallFacets> request;
   
   @Inject
   private Project project;

   @SetupCommand
   public void setup() {
      try
      {
         promptCloneDir();
         promptRepository();
         request.fire(new InstallFacets(GitIgnoreFacet.class));
      }
      catch (Exception e)
      {
         ShellMessages.error(shell, "Failed to create gitignore repository: " + e.getMessage());
      }
   }

   @Command(value = "list", help = "List all available .gitignore templates")
   public void list()
   {
      ShellMessages.info(shell, "Installed .gitignore templates:");
      for (GitIgnoreGroup group : project.getFacet(GitIgnoreFacet.class).list())
      {
         shell.println("============= " + group.getName() +  " =============");
         for (String template : group.getTemplates())
         {
            shell.println(template);
         }
      }
   }
   
   @Command(value = "create", help = "Create a gitignore template")
   public void create(@Option(required = true,
                              completer = GitIgnoreTemplateCompleter.class)
         String... templates)
   {
      FileInputStream in = null;
      try
      {
         GitIgnoreFacet facet = project.getFacet(GitIgnoreFacet.class);
         File gitIgnore = gitIgnore();
         for (String template : templates)
         {
            String content = facet.contentOf(template);
            FileUtils.writeStringToFile(gitIgnore, content, true);
         }
         in = new FileInputStream(gitIgnore);
         ShellMessages.success(shell, "Wrote to .gitignore. Content:");
         shell.println(IOUtils.toString(in));
      }
      catch (Exception e)
      {
         ShellMessages.error(shell, "Failed writing .gitignore: " + e.getMessage());
      }
      IOUtils.closeQuietly(in);
   }
   
   @Command(value = "update-repo", help = "Update the local template repository")
   public void update()
   {
      try
      {
         project.getFacet(GitIgnoreFacet.class).update();
         ShellMessages.success(shell, "Local gitignore repository updated.");
      }
      catch (IOException e)
      {
         ShellMessages.error(shell, "Error reading local repository: " + e.getMessage());
      }
      catch (GitAPIException e)
      {
         ShellMessages.error(shell, "Error pulling remote repository: " + e.getMessage());
      }
   }
   
   private void promptCloneDir()
   {
      FileResource<?> checkout = prompt.promptFile("Where should the gitignore" +
            " template repository be installed at?", defaultDirectory());
      if (checkout.exists()) {
         validate(checkout);
      } else {
         checkout.mkdir();
      }
      config.getScopedConfiguration(ConfigurationScope.USER)
            .setProperty(GitIgnoreFacet.CLONE_LOCATION_KEY, checkout.getFullyQualifiedName());
   }
   
   private void promptRepository()
   {
      String repo = prompt.prompt("Do you want to provide a different repository" +
      		" location for gitignore templates?", GitIgnoreFacet.REPOSITORY);
      config.getScopedConfiguration(ConfigurationScope.USER)
            .setProperty(GitIgnoreFacet.REPOSITORY_KEY, repo);
   }

   private FileResource<?> defaultDirectory()
   {
      File defaultDir = new File(System.getProperty("user.home") + File.separator + ".gitignore_boilerplate");
      return (FileResource<?>) factory.getResourceFrom(defaultDir);
   }

   private void validate(FileResource<?> clone)
   {
      if (!clone.isDirectory())
      {
         throw new IllegalArgumentException("File " + clone + " is not a directory.");
      }
      if (!clone.listResources().isEmpty())
      {
         throw new IllegalArgumentException("Directory " + clone + " is not empty");
      }
   }
   
   @SuppressWarnings("unchecked")
   private File gitIgnore()
   {
      FileResource<?> ignore = project.getProjectRoot().getChildOfType(FileResource.class, ".gitignore");
      if (!ignore.exists())
      {
         ignore.createNewFile();
      }
      return ignore.getUnderlyingResourceObject();
   }

}
