/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.model.file;

import java.util.Date;

import org.opengroup.osdu.core.common.model.file.FileLocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileLocationDoc extends FileLocation {

	String _id;
	String _rev;
	Long createdDate;

	public FileLocationDoc(FileLocation fileLocation) {
		this._id = fileLocation.getFileID();
		this.setCreatedDate(fileLocation.getCreatedAt().getTime());
		this.setCreatedBy(fileLocation.getCreatedBy());
		this.setDriver(fileLocation.getDriver());
		this.setLocation(fileLocation.getLocation());
	}
	
	public FileLocation getFileLocation() {
		return new FileLocation(_id, super.getDriver(), super.getLocation(), new Date(createdDate), super.getCreatedBy());
	}

}
