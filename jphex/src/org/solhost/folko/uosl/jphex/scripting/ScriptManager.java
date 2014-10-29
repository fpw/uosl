/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.uosl.jphex.scripting;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.RubyObject;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.solhost.folko.uosl.jphex.types.Player;
import org.solhost.folko.uosl.libuosl.types.Attribute;
import org.solhost.folko.uosl.libuosl.types.Spell;

public class ScriptManager {
    private static final Logger log = Logger.getLogger("jphex.scripting");
    private static ScriptManager instance;
    private final String basePath;
    private final ScriptingContainer ruby;
    private List<Package> packages;
    private Map<String, TextCommand> commands;
    private Map<String, ItemBehavior> items;
    private Map<String, MobileBehavior> mobiles;
    private Map<Spell, SpellHandler> spells;

    private ScriptManager(String path) {
        this.basePath = path;
        this.packages = new LinkedList<Package>();
        this.ruby = new ScriptingContainer();
        this.commands = new HashMap<String, TextCommand>();
        this.items = new HashMap<String, ItemBehavior>();
        this.mobiles = new HashMap<String, MobileBehavior>();
        this.spells = new HashMap<Spell, SpellHandler>();

        log.config("Using Ruby " + ruby.getCompatVersion());
        Ruby.setThreadLocalRuntime(ruby.getProvider().getRuntime());
        ruby.setCompileMode(CompileMode.FORCE);
        ruby.runScriptlet("require 'yaml'");
        ruby.runScriptlet("java_import " + ScriptAPI.class.getName());
        ruby.runScriptlet("java_import " + ItemBehavior.class.getName());
        ruby.runScriptlet("java_import " + MobileBehavior.class.getName());
        ruby.runScriptlet("java_import " + TextCommand.class.getName());
        ruby.runScriptlet("java_import " + SpellHandler.class.getName());

        // Enumerations
        ruby.runScriptlet("java_import " + Spell.class.getName());
        ruby.runScriptlet("java_import " + Attribute.class.getName());
    }

    public static void init(String path) {
        if(instance != null) {
            throw new RuntimeException("script manager already initialized");
        }
        instance = new ScriptManager(path);
        if(!instance.reload()) {
            throw new RuntimeException("Couldn't compile scripts");
        }
    }

    public static ScriptManager instance() {
        if(instance == null) {
            log.severe("script manager insance requested before existing");
        }
        return instance;
    }

    public void setGlobal(String name, Object obj) {
        ruby.put(name, obj);
    }

    public void addTextCommand(String command, TextCommand script) {
        commands.put(command, script);
    }

    public void addItemClass(String itemName, ItemBehavior itemClass) {
        items.put(itemName, itemClass);
    }

    public void addMobileClass(String mobName, MobileBehavior mobileClass) {
        mobiles.put(mobName, mobileClass);
    }

    public void handleTextCommand(Player player, String line) {
        String args[] = line.split(" ");
        String command = args[0].toLowerCase();
        if(commands.containsKey(command)) {
            try {
                commands.get(command).invoke(player, line.substring(command.length()).trim());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Exception in command " + command + ": " + e.getMessage(), e);
                player.sendSysMessage("Exception in command!");
            }
        } else {
            player.sendSysMessage("Unknown command: " + command);
        }
    }

    public ItemBehavior getItemBehavior(String itemName) {
        return items.get(itemName);
    }

    public MobileBehavior getMobileBehaviour(String mobName) {
        return mobiles.get(mobName);
    }

    Object compileToObject(File file, Class<?> clazz) {
        String className = file.getName().replaceAll("\\.rb", "");
        ruby.runScriptlet(PathType.RELATIVE, file.toString());
        Object receiver = ruby.runScriptlet(className + ".new()");
        return receiver;
    }

    RubyClass compileToClass(File file, Class<?> clazz) {
        String className = file.getName().replaceAll("\\.rb", "");
        ruby.runScriptlet(PathType.RELATIVE, file.toString());
        Object receiver = ruby.runScriptlet(className);
        return (RubyClass) receiver;
    }

    private SpellHandler loadSpell(String path) {
        return (SpellHandler) compileToObject(new File(basePath + path), SpellHandler.class);
    }

    private void loadSpells() {
        log.info("Compiling spells...");
        spells.put(Spell.CREATEFOOD,    loadSpell("/magery/CreateFood.rb"));
        spells.put(Spell.DARKSOURCE,    loadSpell("/magery/Darksource.rb"));
        spells.put(Spell.FIREBALL,      loadSpell("/magery/Fireball.rb"));
        spells.put(Spell.GREATLIGHT,    loadSpell("/magery/GreatLight.rb"));
        spells.put(Spell.HEALING,       loadSpell("/magery/Healing.rb"));
        spells.put(Spell.LIGHT,         loadSpell("/magery/Light.rb"));
        spells.put(Spell.LIGHTSOURCE,   loadSpell("/magery/Lightsource.rb"));
    }

    private void loadPackages() {
        File dir = new File(basePath + "/packages");
        for(File pkgDir : dir.listFiles()) {
            if(pkgDir.isHidden()) continue;
            log.info("Loading package " + pkgDir + "...");
            Package pkg = new Package(this, pkgDir);
            packages.add(pkg);
            pkg.load();
        }
    }

    public ThreadContext getContext() {
        return ruby.getProvider().getRuntime().getCurrentContext();
    }

    public IRubyObject toRubyObject(Object object) {
        return JavaEmbedUtils.javaToRuby(ruby.getProvider().getRuntime(), object);
    }

    public String serialize(RubyObject object) {
        ruby.put("$obj", object);
        Object res = ruby.runScriptlet("YAML::dump($obj)");
        if(res instanceof String) {
            return (String) res;
        } else {
            log.warning("Couldn't serialize " + object);
            return "";
        }
    }

    public RubyObject deserialize(String serialized) {
        ruby.put("$ser", serialized);
        Object res = ruby.runScriptlet("YAML::load($ser)");
        if(res != null) {
            return (RubyObject) toRubyObject(res);
        } else {
            log.warning("Couldn't deserialize ' " + serialized + "'");
            return null;
        }
    }

    public boolean reload() {
        List<Package> oldPackages = new LinkedList<Package>(packages);
        Map<String, TextCommand> oldCommands = new HashMap<String, TextCommand>(commands);
        Map<String, ItemBehavior> oldItems = new HashMap<String, ItemBehavior>(items);
        Map<String, MobileBehavior> oldMobiles = new HashMap<String, MobileBehavior>(mobiles);
        Map<Spell, SpellHandler> oldSpells = new HashMap<Spell, SpellHandler>(spells);

        commands.clear();
        mobiles.clear();
        items.clear();
        packages.clear();
        spells.clear();
        try {
            loadSpells();
            loadPackages();
        } catch(Exception e) {
            log.log(Level.SEVERE, "Couldn't reload scripts: " + e.getMessage(), e);
            packages = oldPackages;
            commands = oldCommands;
            items = oldItems;
            mobiles = oldMobiles;
            spells = oldSpells;
            return false;
        }
        return true;
    }

    public SpellHandler getSpellHandler(Spell spell) {
        return spells.get(spell);
    }
}
