package org.jboss.forge.plugin.gitignore;

import java.util.LinkedList;
import java.util.List;

public class GitIgnoreGroup
{

   private final String name;
   private final List<String> templates;

   public GitIgnoreGroup(String name)
   {
      this(name, new LinkedList<String>());
      
   }
   
   public GitIgnoreGroup(String name, List<String> templates)
   {
      this.name = name;
      this.templates = templates;
   }
   
   public void add(String template)
   {
      templates.add(template);
   }

   public String getName()
   {
      return name;
   }

   public List<String> getTemplates()
   {
      return templates;
   }
   
}
