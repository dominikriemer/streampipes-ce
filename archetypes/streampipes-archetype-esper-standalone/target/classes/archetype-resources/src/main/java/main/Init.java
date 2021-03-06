package ${package}.main;

import java.util.Arrays;

import de.fzi.cep.sepa.desc.ModelSubmitter;

import ${package}.${elementName}.${classNamePrefix}Controller;

public class Init {

	public static void main(String[] args)
	{
		try {
			ModelSubmitter.submitAgent(Arrays.asList(new ${classNamePrefix}Controller()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
