/**
 * This file is part of MythicDrops, licensed under the MIT License.
 *
 * Copyright (C) 2013 Teal Cube Games
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.nunnerycode.bukkit.libraries.ivory.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IvoryStringList extends ArrayList<String> {

    public IvoryStringList(int initialCapacity) {
        super(initialCapacity);
    }

    public IvoryStringList() {
        super();
    }

    public IvoryStringList(Collection<? extends String> c) {
        super(c);
    }

    public IvoryStringList replaceArgs(String[][] args) {
        if (args == null) {
            return this;
        }
        for (int i = 0; i < size(); i++) {
            for (String[] arg : args) {
                set(i, get(i).replace(arg[0], arg[1]));
            }
        }
        return this;
    }

    public IvoryStringList replaceWithList(String key, List<String> list) {
        if (key == null || list == null) {
            return this;
        }
        for (int i = 0; i < size(); i++) {
            if (get(i).equals(key)) {
                remove(i);
                addAll(i, list);
            }
        }
        return this;
    }

}
