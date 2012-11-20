package org.jboss.forge.plugin.gitignore;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.forge.project.Facet;

/**
 * Facet for managing the .gitignore templates.
 */
public interface GitIgnoreFacet extends Facet
{

   public static final String CLONE_LOCATION_KEY = "gitignore.plugin.clone";
   public static final String REPOSITORY_KEY = "gitignore.plugin.repo";
   
   public static final String REPOSITORY = "https://github.com/github/gitignore.git";
   public static final String GITIGNORE = ".gitignore";


   /**
    * List all available gitignore templates.
    */
   List<GitIgnoreGroup> list();

   /**
    * Read the content of a gitignore template
    * @param template         Template name.
    * @return                 Template content as string.
    * @throws IOException     Failure reading the template file.
    */
   String contentOf(String template) throws IOException;

   /**
    * Update the templates from the remote repository.
    * @throws IOException     Failure reading the git repository.
    * @throws GitAPIException Git failure.
    */
   void update() throws IOException, GitAPIException;

}
