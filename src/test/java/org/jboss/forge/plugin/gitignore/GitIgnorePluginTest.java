package org.jboss.forge.plugin.gitignore;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class GitIgnorePluginTest extends AbstractShellTest
{

   @Deployment
   public static JavaArchive getDeployment()
   {
      return AbstractShellTest.getDeployment().addPackages(true, GitIgnorePlugin.class.getPackage());
   }

   @Test
   public void should_setup_gibo() throws Exception
   {
      // given
      initializeJavaProject();
      Resource<?> cloneFolder = cloneFolder();
      queueInputLines(cloneFolder.getFullyQualifiedName(), "\n");

      // when
      getShell().execute("git-ignore setup");

      // then
      List<Resource<?>> resources = cloneFolder.listResources();
      assertNotNull(resources);
      assertFalse(resources.isEmpty());

      int counter = 0;
      for (Resource<?> resource : resources)
      {
         String name = resource.getName();
         if (name != null && name.endsWith(".gitignore"))
         {
            counter++;
         }
      }
      assertTrue(counter > 0);
   }
   
   @Test
   public void should_update_gibo() throws Exception
   {
      // given
      initializeJavaProject();
      Resource<?> cloneFolder = cloneFolder();
      queueInputLines(cloneFolder.getFullyQualifiedName(), "\n");

      // when
      getShell().execute("git-ignore setup");
      getShell().execute("git-ignore update-repo");

      // then
      assertTrue(getOutput().contains("Local gitignore repository updated"));
   }
   
   @Test
   public void should_list_templates() throws Exception
   {
      // given
      initializeJavaProject();
      Resource<?> cloneFolder = cloneFolder();
      queueInputLines(cloneFolder.getFullyQualifiedName(), "\n");

      // when
      getShell().execute("git-ignore setup");
      getShell().execute("git-ignore list");

      // then
      String listOutput = getOutput().substring(getOutput().indexOf("==="));
      assertFalse(listOutput.contains(".gitignore"));
      assertTrue(listOutput.contains("= Languages ="));
      assertTrue(listOutput.contains("= Globals ="));
      assertTrue(listOutput.contains("Java"));
      assertTrue(listOutput.contains("Eclipse"));
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void should_create_gitignore() throws Exception
   {
   // given
      initializeJavaProject();
      Resource<?> cloneFolder = cloneFolder();
      queueInputLines(cloneFolder.getFullyQualifiedName(), "\n");

      // when
      getShell().execute("git-ignore setup");
      getShell().execute("git-ignore create Java Maven");

      // then
      FileResource<?> gitignore = getProject().getProjectRoot().getChildOfType(FileResource.class, ".gitignore");
      assertTrue(gitignore.exists());
      String content = IOUtils.toString(new FileInputStream(gitignore.getUnderlyingResourceObject()));
      assertTrue(content.contains("*.class"));
      assertTrue(content.contains("target/"));
   }

   private Resource<?> cloneFolder()
   {
      return getProject().getProjectRoot().getChildDirectory("gibo");
   }
}
