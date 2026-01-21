/*
 * Aws S3 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    private Validator() {
    }

    public static void stringValidator(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void fileValidator(File file, String errorMessage) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static String extractURLFromAnchorTag(String htmlString) {
        // Regular expression to find href URL within the anchor tag
        String regex = "href\\s*=\\s*['\"]([^'\"]+)['\"]|href\\s*=\\s*([^\\s>]+)";

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlString);

        // Check if a match is found
        if (matcher.find()) {
            // Extract the matched group
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        } else {
            // Return null if no URL is found
            return null;
        }
    }
}

