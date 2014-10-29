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

public class Package {
    private final ScriptManager scripts;
    private final File path;

    public Package(ScriptManager manager, File path) {
        this.scripts = manager;
        this.path = path;
    }

    public void load() {
        loadCommands();
        loadItems();
        loadMobiles();
    }

    private void loadCommands() {
        File dir = new File(path + "/commands");
        if(!dir.exists()) return;

        for(File cmdFile : dir.listFiles()) {
            if(!cmdFile.toString().endsWith(".rb")) continue;
            TextCommand script = (TextCommand) scripts.compileToObject(cmdFile, TextCommand.class);
            String className = cmdFile.getName().replaceAll("\\.rb", "");
            scripts.addTextCommand(className.toLowerCase(), script);
        }
    }

    private void loadMobiles() {
        File dir = new File(path + "/mobiles");
        if(!dir.exists()) return;

        for(File cmdFile : dir.listFiles()) {
            if(!cmdFile.toString().endsWith(".rb")) continue;
            Object script = scripts.compileToObject(cmdFile, MobileBehavior.class);
            String className = cmdFile.getName().replaceAll("\\.rb", "");
            scripts.addMobileClass(className.toLowerCase(), (MobileBehavior) script);
        }
    }

    private void loadItems() {
        File dir = new File(path + "/items");
        if(!dir.exists()) return;

        for(File cmdFile : dir.listFiles()) {
            if(!cmdFile.toString().endsWith(".rb")) continue;
            Object script = scripts.compileToObject(cmdFile, ItemBehavior.class);
            String className = cmdFile.getName().replaceAll("\\.rb", "");
            scripts.addItemClass(className.toLowerCase(), (ItemBehavior) script);
        }
    }
}
