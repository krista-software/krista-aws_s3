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

package app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities;

import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_b77b40c4-1319-4b32-9b55-07d360453974",
        name = "Cloud Storage",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "aa8bb7b1-4055-4598-805b-57410ea4c2b6")
@Entity(name = "File Details", id = "localDomainEntity_5c69866f-2503-4f88-a4eb-c9a41b3943bb", primaryKey = "File Name", supportStore = false, options = {})
public class FileDetails {


    @Searchable
    @Field(name = "File Name", type = "ProperNoun", required = true, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String fileName;

    @Field(name = "Size", type = "Number", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long size;

    @Searchable
    @Field(name = "Bucket Name", type = "ProperNoun", required = true, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String bucketName;

    @Field.Date(name = "Last Modified", required = true, includeTimeOfDay = false, showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long lastModified;

    @Searchable
    @Field(name = "Owner", type = "Person", required = true, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String owner;

    @Field.Text(name = "Tag", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String tag;

    @ToString
    @Field.Boolean(name = "Is Folder", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean isFolder;

    @Field.Text(name = "Storage Class", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String storageClass;

}