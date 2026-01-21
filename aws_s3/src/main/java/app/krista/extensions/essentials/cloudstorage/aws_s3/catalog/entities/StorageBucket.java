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

@Searchable
@Domain(id = "catEntryDomain_b77b40c4-1319-4b32-9b55-07d360453974",
        name = "Cloud Storage",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "37e3eb92-c230-41db-b459-59fd73bfb20e")
@Entity(name = "Storage Bucket", id = "localDomainEntity_ad327ad4-082d-41e5-9d65-74ef3d4a9cf2", primaryKey = "Name", supportStore = true)
public class StorageBucket {

    @ToString
    @Field.Text(name = "Id", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String id;

    @Searchable
    @ToString
    @Field(name = "Name", type = "Text", attributes = {@Attribute(name = "visualWidth", value = "M")})
    public String name;

    @Field(name = "Owner", type = "Person", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")})
    public String owner;

    @Field.Date(name = "Created Date", includeTimeOfDay = false, showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Long createdDate;


}