/**
 * ObjectViewer is a tool allowing to search and view elements in a graph of objects.
 * <p>
 * Copyright (C) 2016-2016 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
 * <p>
 * ObjectViewer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * ObjectViewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package fr.duminy.objectviewer.api;

import net.java.sezpoz.Indexable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author Fabien DUMINY
 */
@Target({ TYPE, METHOD, FIELD })
@Retention(SOURCE)
@Indexable(type = ObjectLoaderService.class)
public @interface ObjectLoader {
}
