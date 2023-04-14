/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "org.opengroup.osdu")
@PropertySource("classpath:swagger.properties")
public class FileIBMApplication {

  public static void main(String[] args) {
	  //BasicConfigurator.configure();
    SpringApplication.run(FileIBMApplication.class, args);
  }

}
