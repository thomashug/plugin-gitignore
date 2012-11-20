package org.jboss.forge.plugin.gitignore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.parser.java.util.Strings;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;

public class GitIgnoreFacetDefault extends BaseFacet implements GitIgnoreFacet
{
   private static final String GLOBAL_TEMPLATES = "Global";

   @Inject
   private Configuration config;
   
   @Inject
   private ResourceFactory factory;
   
   @Inject
   private Shell shell;

   @Override
   public boolean install()
   {
      try
      {
         DirectoryResource cloneDir = cloneDir();
         String repo = repoUrl();
         ShellMessages.info(shell, "Cloning " + repo + " into " + cloneDir.getFullyQualifiedName());
         Git.cloneRepository().setURI(repo)
            .setDirectory(cloneDir.getUnderlyingResourceObject())
            .call();
         return true;
      }
      catch (Exception e)
      {
         ShellMessages.error(shell, "Failed to checkout gitignore: " + e);
         return false;
      }
   }

   @Override
   public boolean isInstalled()
   {
      String location = config.getString(CLONE_LOCATION_KEY);
      if (Strings.isNullOrEmpty(location)) {
         return false;
      }
      File clone = new File(location);
      Resource<File> cloneDir = factory.getResourceFrom(clone);
      return cloneDir.exists() && cloneDir.getChild(".git").exists();
   }
   
   @Override
   public List<GitIgnoreGroup> list()
   {
      List<GitIgnoreGroup> result = new ArrayList<GitIgnoreGroup>(2);
      DirectoryResource languages = cloneDir();
      result.add(new GitIgnoreGroup("Languages", listGitignores(languages)));
      result.add(new GitIgnoreGroup("Globals", listGitignores(languages.getChildDirectory(GLOBAL_TEMPLATES))));
      return result;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public String contentOf(String template) throws IOException
   {
      DirectoryResource[] candidates = new DirectoryResource[] {
               cloneDir(), cloneDir().getChildDirectory(GLOBAL_TEMPLATES)
      };
      for (DirectoryResource dir : candidates)
      {
         if (listGitignores(dir).contains(template))
         {
            FileResource<?> file = dir.getChildOfType(FileResource.class, template + GITIGNORE);
            return IOUtils.toString(file.getResourceInputStream());
         }
      }
      return "";
   }
   
   @Override
   public void update() throws IOException, GitAPIException
   {
      RepositoryBuilder db = new RepositoryBuilder().findGitDir(cloneDir().getUnderlyingResourceObject());
      Git git = new Git(db.build());
      git.pull()
         .setTimeout(10000)
         .setProgressMonitor(new TextProgressMonitor())
         .call();
   }
   
   private List<String> listGitignores(DirectoryResource dir)
   {
      List<String> result = new LinkedList<String>();
      ResourceFilter filter = new ResourceFilter()
      {
         @Override
         public boolean accept(Resource<?> resource)
         {
            return resource.getName().endsWith(GITIGNORE);
         }
      };
      for (Resource<?> resource : dir.listResources(filter))
      {
         String name = resource.getName();
         String cut = name.substring(0, name.indexOf(GITIGNORE));
         result.add(cut);
      }
      return result;
   }
   
   private DirectoryResource cloneDir()
   {
      return new DirectoryResource(factory, cloneLocation());
   }

   private File cloneLocation()
   {
      return new File(config.getScopedConfiguration(ConfigurationScope.USER).getString(CLONE_LOCATION_KEY));
   }
   
   private String repoUrl() {
      return config.getScopedConfiguration(ConfigurationScope.USER).getString(REPOSITORY_KEY);
   }

}
