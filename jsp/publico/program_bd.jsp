<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<logic:present name="siteView" property="component" > 
	<bean:define id="component" name="siteView" property="component"/>
	<logic:empty name="component" property="program">
		<h2><bean:message key="message.program.not.available"/></h2>
	</logic:empty>
	<logic:notEmpty name="component" property="program">
	<table>
		<tr>
			<td>
				<h2><bean:message key="label.program" /></h2>	
			</td>
		</tr>
		<tr>
			<td>
				<bean:write name="component" property="program" filter="false"/>
			</td>
		</tr>
	</table>
	</logic:notEmpty>
</logic:present>
<logic:notPresent name="siteView" property="component" >
<h2><bean:message key="message.program.not.available"/>
</h2>
</logic:notPresent>