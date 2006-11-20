
/*
 * Copyright  2004-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.trinidadinternal.skin;

import java.io.IOException;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;

import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

import javax.servlet.ServletContext;
import javax.xml.parsers.SAXParserFactory;

import org.apache.myfaces.trinidad.skin.SkinFactory;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;


import org.apache.myfaces.trinidad.skin.Skin;
import org.apache.myfaces.trinidadinternal.renderkit.core.skin.MinimalDesktopSkinExtension;
import org.apache.myfaces.trinidadinternal.renderkit.core.skin.MinimalPdaSkinExtension;
import org.apache.myfaces.trinidadinternal.renderkit.core.skin.SimpleDesktopSkin;
import org.apache.myfaces.trinidadinternal.renderkit.core.skin.SimplePdaSkin;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.myfaces.trinidadinternal.share.io.NameResolver;
import org.apache.myfaces.trinidadinternal.share.xml.ClassParserFactory;
import org.apache.myfaces.trinidadinternal.share.xml.ParseContextImpl;
import org.apache.myfaces.trinidadinternal.share.xml.ParserFactory;
import org.apache.myfaces.trinidadinternal.share.xml.ParserManager;
import org.apache.myfaces.trinidadinternal.share.xml.TreeBuilder;
import org.apache.myfaces.trinidadinternal.share.xml.XMLProvider;
import org.apache.myfaces.trinidadinternal.share.xml.XMLUtils;

import org.apache.myfaces.trinidadinternal.ui.laf.xml.XMLConstants;
import org.apache.myfaces.trinidadinternal.ui.laf.xml.parse.SkinAdditionNode;
import org.apache.myfaces.trinidadinternal.ui.laf.xml.parse.SkinNode;
import org.apache.myfaces.trinidadinternal.ui.laf.xml.parse.SkinsNode;

/**
 * Utility functions for creating Skins from the trinidad-skins.xml file
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/skin/SkinUtils.java#0 $) $Date: 10-nov-2005.18:59:00 $
 * @author The Oracle ADF Faces Team
 */
public class SkinUtils
{

  /**
   * Register the base skins with the SkinFactory. (simple/minimal)
   * Make sure the SkinFactory.getFactory() does not return null before
   * calling this method.
   */
  static public void registerBaseSkins()
  {

    SkinFactory skinFactory = SkinFactory.getFactory();

    // skinFactory should be non-null when this is called since it is
    // initiated in the TrinidadFilterImpl, but in case it isn't do this
    if (skinFactory == null)
    {
      SkinFactory.setFactory(new SkinFactoryImpl());
      skinFactory = SkinFactory.getFactory();
    }

    _registerTrinidadSkins(skinFactory);
  }
  
  /**
   * Register any custom skin extensions found in the
   * trinidad-skins.xml file with the SkinFactory.
   * 
   * Make sure the SkinFactory.getFactory() does not return null before
   * calling this method.
   * You should call registerBaseSkins() before calling this method.
   * @param context ServletContext, used to get the trinidad-skins.xml file.
   */
  static public void registerSkinExtensions(
    ServletContext context)
  {

    SkinFactory skinFactory = SkinFactory.getFactory();

    // skinFactory should be non-null when this is called since it is
    // initiated in the TrinidadFilterImpl, but in case it isn't do this
    if (skinFactory == null)
    {
      SkinFactory.setFactory(new SkinFactoryImpl());
      skinFactory = SkinFactory.getFactory();
    }

    _registerSkinExtensions(context, skinFactory);

  }

  /**
   * Create a SkinExtension off a generic SAX input source, using
   * a custom parsing manager.
   * <p>
   * @param provider
   * @param resolver A NameResolver that can be used to locate
   *                 resources, such as source images for colorized
   *                 icons.
   * @param parserManager the ParserManager to use for parsing
   *                Must  be non-null.
   * @throws NullPointerException when inputStream or parserManager
   *         is null.
   */
  /**
   * 
   * @param provider an XMLProvider implementation.
   * @param resolver A NameResolver that can be used to locate
   *                 resources, such as source images for colorized
   *                 icons.
   * @param inputStream the inputStream. Must be non-null
   * @param parserManager the ParserManager to use for parsing
   *                Must  be non-null.
   * @param configFile The name of the config file we are parsing.
   * @return A SkinsNode object (contains a List of SkinNode and a List of SkinAdditionNode)
   */
  static private SkinsNode _getSkinsNodeFromInputStream(
    XMLProvider        provider,
    NameResolver       resolver,
    InputStream        inputStream,
    ParserManager      parserManager,
    String             configFile
    )
  {
  
    if (inputStream == null)
      throw new NullPointerException("Null inputStream");
    if (parserManager == null)
      throw new NullPointerException("Null parserManager");
    SkinsNode skinsNode = null;
    try
    {
      InputSource input = new InputSource();
      input.setByteStream(inputStream);
      input.setPublicId(configFile);

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);   
      
      ParseContextImpl context = new ParseContextImpl();

      // Set up the NameResolver if we have one
      if (resolver != null)
        XMLUtils.setResolver(context, resolver);

      // Create the TreeBuilder
      TreeBuilder builder = new TreeBuilder(parserManager,
                                            SkinsNode.class);
      skinsNode = ((SkinsNode)builder.parse(provider, input, context));
   
    }
    catch (IOException ioe)
    {
      _LOG.warning(ioe);
    }
    catch (SAXException saxe)
    {
      _LOG.warning(saxe);
    }
    finally
    {
      try
      {
        inputStream.close();
      }
      catch (IOException ioe)
      {
        // Ignore
        ;
      }
    }
    return skinsNode;
  }

  /**
   * Creates a ParserManager pre-registered witih all
   * the default ParserFactories needed to create SkinExtensions.
   */
  static public ParserManager createDefaultManager()
  {
    ParserManager manager = new ParserManager();

    // Register top-level factory
     _registerFactory(manager, SkinsNode.class, "SkinsNode");
    
    // Register skin node factory and skin addition node factory
    _registerFactory(manager, SkinNode.class, "SkinNode");
    _registerFactory(manager, SkinAdditionNode.class, "SkinAdditionNode");

    return manager;
  }

  // Returns a singleton instance of the default ParserManager
  static private ParserManager _getDefaultManager()
  {
    if (_sManager == null)
      _sManager = createDefaultManager();

    return _sManager;
  }

  // Registers a ParserFactory for the LAF namespace
  static private void _registerFactory(
    ParserManager manager,
    Class<?> expectedType,
    String baseName
    )
  {
    String className = _LAF_PARSE_PACKAGE + baseName + "Parser";
    ParserFactory factory = new ClassParserFactory(className);

    manager.registerFactory(expectedType,
                            XMLConstants.SKIN_NAMESPACE,
                            factory);
  }

  /**
   * register the Trinidad skins: simpleDesktopSkin, simplePdaSkin,
   * and minimalDesktopSkin, minimalPdaSkin, and blafPlusDesktopSkin.
   * @param skinFactory
   */
  private static void _registerTrinidadSkins(
    SkinFactory skinFactory)
  {
    // SimpleDesktopSkin is the BASE skin for org.apache.myfaces.trinidad.desktop renderKit
    // SimplePdaSkin is the BASE skin for org.apache.myfaces.trinidad.pda renderKit. By
    // BASE skin, I mean, this is the skin that all SkinExtensions extend
    // from.
    SimpleDesktopSkin simpleDesktopSkin = new SimpleDesktopSkin();
    skinFactory.addSkin(simpleDesktopSkin.getId(), simpleDesktopSkin);

    SimplePdaSkin simplePdaSkin = new SimplePdaSkin();
    skinFactory.addSkin(simplePdaSkin.getId(), simplePdaSkin);

    MinimalDesktopSkinExtension minimalDesktopSkin =
      new MinimalDesktopSkinExtension(simpleDesktopSkin);
    skinFactory.addSkin(minimalDesktopSkin.getId(), minimalDesktopSkin);

    MinimalPdaSkinExtension minimalPdaSkin =
      new MinimalPdaSkinExtension(simplePdaSkin);
    skinFactory.addSkin(minimalPdaSkin.getId(), minimalPdaSkin);
  }

  /**
   * Parse the trinidad-skins.xml file for SkinExtensions and add each
   * SkinExtension to the skinFactory.
   * First find all the trinidad-skins.xml files that are in META-INF directory, and 
   * add those skins to the skin factory.
   * Then find the WEB-INF/trinidad-skins.xml file and add those skins to the skin factory.
   * The skins are ordered so that the 'extended' skins are registered before the skins that extend
   * them.
   * @param context
   * @param skinFactory
   */
  private static void _registerSkinExtensions(
    ServletContext context,
    SkinFactory skinFactory)
  {
    if (context == null)
      return;
     
    // Add META-INF/trinidad-skins.xml skins to skin factory. (sorted first to make sure 
    // we register the most 'base' skins first)
    List<SkinsNode> metaInfSkinsNodeList = _getMetaInfSkinsNodeList(); 
    // Go through each SkinsNode object 
    // (contains List of SkinNodes and List of SkinAdditionNodes)
    // and return a List of the SkinNodes.
    List<SkinNode> metaInfSkinNodes = new ArrayList<SkinNode>();
    for (SkinsNode skinsNode : metaInfSkinsNodeList)
    {
      metaInfSkinNodes.addAll(skinsNode.getSkinNodes());
    }    
    
    List<SkinNode> sortedMetaInfSkinNodes = _sortSkinNodes(skinFactory, metaInfSkinNodes);
    
    for (SkinNode skinNode : sortedMetaInfSkinNodes)
    {
      _addSkinToFactory(skinFactory, skinNode, true);
    }
    
    // Add WEB-INF/trinidad-skins.xml skins to skin factory. (sorted first)    
    SkinsNode webInfSkinsNode = _getWebInfSkinsNode(context);
    if (webInfSkinsNode != null)
    {
      List<SkinNode> webInfSkinNodes = webInfSkinsNode.getSkinNodes();
  
      List<SkinNode> sortedWebInfSkinNodes = _sortSkinNodes(skinFactory, webInfSkinNodes);
      
      // register skins found in webInfSkinNodes
      for (SkinNode skinNode : sortedWebInfSkinNodes)
      {
        _addSkinToFactory(skinFactory, skinNode, false);
      }
    }
    
    // register all the skin additions from META-INF trinidad-skins.xml and WEB-INF
    // trinidad-skins.xml that we have stored in the metaInfSkinsNodeList object and the
    // webInfSkinsNode object
    FacesContext fContext = FacesContext.getCurrentInstance();
    // register skin-additions from META-INF/trinidad-skins.xml files
    for (SkinsNode skinsNode : metaInfSkinsNodeList)
    {
      List<SkinAdditionNode> skinAdditionNodeList = skinsNode.getSkinAdditionNodes();
      _registerSkinAdditions(fContext, skinFactory, skinAdditionNodeList, true);    
    } 
    // register skin-additions from WEB-INF/trinidad-skins.xml file
    if (webInfSkinsNode != null)
    {
      List<SkinAdditionNode> skinAdditionNodeList = webInfSkinsNode.getSkinAdditionNodes();
      _registerSkinAdditions(fContext, skinFactory, skinAdditionNodeList, false);    
    }
    
  }
  
  /**
   * Given the a List of SkinNodes, sort them so that the SkinNodes in such a way so that
   * when we register the skins we make sure that the 'base' skins are registered before
   * skins that extend them.
   * @param skinFactory
   * @param skinNodes
   * @return sorted List of SkinNodes
   */
  private static List<SkinNode> _sortSkinNodes(
    SkinFactory    skinFactory,
    List<SkinNode> skinNodes)
  {
    List<SkinNode> sortedSkinNodes = new ArrayList<SkinNode>();
    List<String>   skinNodesAdded = new ArrayList<String>();
    List<String>   baseSkinIds = new ArrayList<String>();
    for (Iterator i=skinFactory.getSkinIds(); i.hasNext(); ) 
       baseSkinIds.add((String)i.next());
   
    // first, the skins that don't extend anything
    for (SkinNode skinNode : skinNodes)
    {
      String skinExtends = skinNode.getSkinExtends();

      if (skinExtends == null)
      {
        sortedSkinNodes.add(skinNode);
        skinNodesAdded.add(skinNode.getId());
      }
    }
    
    // second, the skins that extend another skin
    _sortSkinNodesWithExtensions(skinNodes, sortedSkinNodes, skinNodesAdded, baseSkinIds, 0);
      
    return sortedSkinNodes;
    
  }
  
  /**
   * This sorts SkinNodes that have their 'extends' value set, which means the skin
   * extends another skin. The order of our skin nodes matters in this case. We want the 
   * base skin to be registered first.
   * @param skinNodes
   * @param sortedSkinNodes
   * @param skinNodesAdded
   * @param baseSkinIds
   */
  private static void _sortSkinNodesWithExtensions(
    List<SkinNode> skinNodes,
    List<SkinNode> sortedSkinNodes,
    List<String>   skinNodesAdded,
    List<String>   baseSkinIds,
    int            originalLeftOverListSize)
  {
    List<SkinNode> leftOverList = new ArrayList<SkinNode>();
  
    for (SkinNode skinNode : skinNodes)
    {
      String skinExtends = skinNode.getSkinExtends();

      if (skinExtends != null)
      {
        if (skinNodesAdded.contains(skinExtends) ||
            baseSkinIds.contains(skinExtends))
        {
          sortedSkinNodes.add(skinNode);
          skinNodesAdded.add(skinNode.getId());
        }
        else
        { 
          // left over, put in a left-over list
          leftOverList.add(skinNode);
        }  
      }
    } 
    if ((originalLeftOverListSize > 0) && 
         (leftOverList.size() == originalLeftOverListSize))
    {
      // Ok, we are left with skinNodes that cannot be registered because the skin they extend is
      // not in the skinNodesAdded List. The skin they extend might not exist at all, or 
      // there is a circular dependency.
      // So..., just add these to the list. When we register these, they will cause a severe error
      // and the default base skin will be used.
       StringBuffer buffer = new StringBuffer();
       for (SkinNode leftOverNode : leftOverList)
       {
         buffer.append("Skin with id: " + leftOverNode.getId() + 
                      " extends skin with id: " + leftOverNode.getSkinExtends() + "\n");
         sortedSkinNodes.add(leftOverNode);
         skinNodesAdded.add(leftOverNode.getId());

       }
      _LOG.warning("The following skins extend each other in a circular " +
                   "fashion or the skin they extend does not exist.\n" + buffer.toString());
 
    } 
    else  if (leftOverList.size() > 0)   
    {
      _sortSkinNodesWithExtensions(leftOverList, sortedSkinNodes, 
                                   skinNodesAdded, baseSkinIds, leftOverList.size());      
    }
  }
 
  /**
   * Given a skinNode, create a Skin object and 
   * register the SkinExtension object with the skinFactory
   * @param skinFactory
   * @param skinNode
   */
  private static void _addSkinToFactory(
    SkinFactory skinFactory, 
    SkinNode    skinNode,
    boolean     isMetaInfFile)
  {
    // if the renderKitId is not specified,
    // set it to _RENDER_KIT_ID_CORE.
    String renderKitId = skinNode.getRenderKitId();
    String id = skinNode.getId();
    String family = skinNode.getFamily();
    String styleSheetName = skinNode.getStyleSheetName();
    String bundleName = skinNode.getBundleName();
    
    if (renderKitId == null)
      renderKitId = _RENDER_KIT_ID_DESKTOP;


    // figure out the base skin.
    Skin baseSkin = null;
    String skinExtends = skinNode.getSkinExtends();
    
    if (skinExtends != null)
      baseSkin = skinFactory.getSkin(null, skinExtends);
    if (baseSkin == null)
    {
      baseSkin = _getDefaultBaseSkin(skinFactory, renderKitId);
      
      if (skinExtends != null)
      {
        _LOG.severe("Unable to locate base skin \"{0}\" for " +
                    "use in defining skin of id \"{1}\", family " +
                    "\"{2}\", renderkit ID \"{3}\". Using the default base skin \"{4}\".",
                    new String[]{skinExtends, id, family, renderKitId, baseSkin.getId()});
      }

    }

    SkinExtension skin = new SkinExtension(baseSkin,
                                           id,
                                           family,
                                           renderKitId);

    // Set the style sheet
    if (styleSheetName != null)
    {
      // If the styleSheetName is in the META-INF/trinidad-skins.xml file, then
      // we prepend META-INF to the styleSheetName if it doesn't begin with '/'.
      // This way we can find the file when we go to parse it later.
      if (isMetaInfFile)
        styleSheetName = _prependMetaInf(styleSheetName);
      skin.setStyleSheetName(styleSheetName);
    }
    // And the bundle
    if (bundleName != null)
      skin.setBundleName(bundleName);

    // Create a SkinExtension object and register skin with factory
    skinFactory.addSkin(id, skin);    
  }
  
  /**
   * Get the WEB-INF/trinidad-skins.xml file, parse it, and return a List SkinNode objects. 
   * @param context ServletContext used to getResourceAsStream
   * @return List of SkinNodes (skin elements) found in trinidad-skins.xml
   */
  private static SkinsNode _getWebInfSkinsNode(
    ServletContext context)
  {
    InputStream in = context.getResourceAsStream(_CONFIG_FILE);
    if (in != null)
    {
      SkinsNode webInfSkinsNode = 
        _getSkinsNodeFromInputStream(null, null, in, _getDefaultManager(), _CONFIG_FILE);

      return webInfSkinsNode;
    }
    else
    {
      return null;
    }
  }
 
  /**
   * Get all the META-INF/trinidad-skins.xml files, parse them, and from each file we get
   * a SkinsNode object -- the information inside the &lt;skins&gt; element -- each skin
   * and each skin-addition.
   * @return Each SkinsNode object we get from each META-INF/trinidad-skins.xml file, 
   * in a List<SkinsNode>.
   */
  private static List<SkinsNode> _getMetaInfSkinsNodeList()
  {
        
    List<SkinsNode> allSkinsNodes = new ArrayList<SkinsNode>(); 
    ClassLoader loader = Thread.currentThread().getContextClassLoader();     
    
    try
    {
  
      Enumeration<URL> urls = loader.getResources(_META_INF_CONFIG_FILE);
      while (urls.hasMoreElements())
      {
        URL url = urls.nextElement();
        
        _LOG.finest("Processing:{0}", url);
        try
        {
          // parse the config file and register the skin's additional stylesheets.
          InputStream in = url.openStream();
          if (in != null)
          {
            SkinsNode  metaInfSkinsNode = 
              _getSkinsNodeFromInputStream(null, null, in, _getDefaultManager(), 
                                           _META_INF_CONFIG_FILE);
              
            allSkinsNodes.add(metaInfSkinsNode);
          }
        }
        catch (Exception e)
        {
         _LOG.warning("Error parsing:"+url, e);
        }
      }
    }
    catch (IOException e)
    {
      _LOG.severe("error loading file:"+ _META_INF_CONFIG_FILE, e);
    }
    
    return allSkinsNodes;
  } 

  private static Skin _getDefaultBaseSkin(
    SkinFactory factory,
    String      renderKitId)
  {

    String baseSkinId = (_RENDER_KIT_ID_PDA.equals(renderKitId)) ?
                          _SIMPLE_PDA_SKIN_ID :
                          _SIMPLE_DESKTOP_SKIN_ID;

    Skin baseSkin = factory.getSkin(null, baseSkinId);

    // It is an error if we were unable to find the base skin
    if (baseSkin == null)
      _LOG.severe(_UNKNOWN_BASE_SKIN_ERROR + baseSkinId);

    return baseSkin;
  }
  
  /**
   * Get the skin id and stylesheet name from each SkinAdditionNode and
   * get the skin and register the styleSheetName with the skin
   * @param fContext
   * @param skinFactory
   * @param skinAdditionNodeList
   * @param isMetaInfFile true if the trinidad-skins.xml file is in the META-INF
   * directory.
   */
  private static void _registerSkinAdditions(
    FacesContext fContext,
    SkinFactory  skinFactory,
    List<SkinAdditionNode> skinAdditionNodeList,
    boolean      isMetaInfFile
    )
  {
    for (SkinAdditionNode skinAdditionNode : skinAdditionNodeList)
    {
      String skinId = skinAdditionNode.getSkinId();
      String styleSheetName = skinAdditionNode.getStyleSheetName();
  
      Skin skin = skinFactory.getSkin(fContext, skinId);
      if (skin != null && styleSheetName != null)
      {  
        // If the styleSheetName is in the META-INF/trinidad-skins.xml file, then
        // we prepend META-INF to the styleSheetName if it doesn't begin with '/'.
        // This way we can find the file when we go to parse it later.
        if (isMetaInfFile)
          styleSheetName = _prependMetaInf(styleSheetName);
        skin.registerStyleSheet(styleSheetName); 
      }
    }    
  }

  /**
   * Prepend META-INF to the styleSheetName if it doesn't begin with '/'.
   * @param styleSheetName
   * @return String styleSheetName or the styleSheetName prepended with META-INF/
   */
  private static String _prependMetaInf(String styleSheetName)
  {
    if (!(styleSheetName.startsWith("/")))
      return _META_INF_DIR.concat(styleSheetName);
    else
      return styleSheetName;
  }
  
  private SkinUtils() {}

  // The default ParserManager
  static private ParserManager _sManager;

  // Constants

  // Prefix of LAf parsing package
  static private final String _LAF_PARSE_PACKAGE =
    "org.apache.myfaces.trinidadinternal.ui.laf.xml.parse.";


  static private final String _CONFIG_FILE = "/WEB-INF/trinidad-skins.xml";
  static private final String _META_INF_CONFIG_FILE = "META-INF/trinidad-skins.xml";
  static private final String _META_INF_DIR = "META-INF/";
  static private final TrinidadLogger _LOG = TrinidadLogger.createTrinidadLogger(SkinUtils.class);

  // Error messages
  private static final String _UNKNOWN_BASE_SKIN_ERROR =
    "Unable to locate base skin: ";

  static private final String _RENDER_KIT_ID_DESKTOP = "org.apache.myfaces.trinidad.desktop";
  static private final String _RENDER_KIT_ID_PDA = "org.apache.myfaces.trinidad.pda";
  static private final String _SIMPLE_PDA_SKIN_ID = "simple.pda";
  static private final String _SIMPLE_DESKTOP_SKIN_ID = "simple.desktop";

}
