<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<html:xhtml/>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/taglibs-datetime.tld" prefix="dt" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2>
	<bean:message key="link.program"/>
</h2>

	<bean:define id="executionPeriod" type="net.sourceforge.fenixedu.domain.ExecutionPeriod"
			name="executionCourse" property="executionPeriod"/>

<logic:iterate id="entry" name="executionCourse" property="curricularCoursesIndexedByCompetenceCourse">
	<bean:define id="competenceCourse" name="entry" property="key"/>
	<logic:equal name="competenceCourse" property="curricularStage.name" value="APPROVED">
		<h3>
			<fr:view name="competenceCourse" property="nameI18N"/>
			<br/>
			<logic:iterate id="curricularCourse" name="entry" property="value" indexId="i">
				<logic:notEqual name="i" value="0"><br/></logic:notEqual>
				<bean:define id="degree" name="curricularCourse" property="degreeCurricularPlan.degree"/>
				<bean:message bundle="ENUMERATION_RESOURCES" name="degree" property="degreeType.name"/>
				<bean:message key="label.in"/>
				<fr:view name="degree" property="nameI18N">
					<fr:layout>
						<fr:property name="escaped" value="false" />
						<fr:property name="newlineAware" value="false" />
					</fr:layout>
				</fr:view>
			</logic:iterate>
		</h3>
		<blockquote>
			<h4>
				<bean:message key="title.program"/>
			</h4>
			<fr:view name="competenceCourse" property="programI18N" layout="html"/>
		</blockquote>
	</logic:equal>
</logic:iterate>

	<logic:iterate id="curricularCourse" type="net.sourceforge.fenixedu.domain.CurricularCourse"
			name="executionCourse" property="curricularCoursesSortedByDegreeAndCurricularCourseName">
		<bean:define id="degree" name="curricularCourse" property="degreeCurricularPlan.degree"/>
		<logic:notEqual name="curricularCourse" property="isBolonha" value="true">
			<% net.sourceforge.fenixedu.domain.Curriculum curriculum = curricularCourse.findLatestCurriculumModifiedBefore(executionPeriod.getExecutionYear().getEndDate()); %>
			<% net.sourceforge.fenixedu.domain.Curriculum lastCurriculum = curricularCourse.findLatestCurriculum(); %>
			<% request.setAttribute("curriculum", curriculum); %>
			<% request.setAttribute("lastCurriculum", lastCurriculum); %>

				<h3 class="mtop2">
					<%-- <fr:view name="curricularCourse" property="nameI18N"/> --%>

					<bean:message bundle="ENUMERATION_RESOURCES" name="degree" property="degreeType.name"/>
					<bean:message key="label.in"/>
					<fr:view name="degree" property="nameI18N"/>
				</h3>
					<logic:present name="curriculum">
						<h4 class="mbottom05 greytxt">
							<bean:message key="title.program"/>
						</h4>
						<div class="mtop05" style="line-height: 1.5em;">
						<fr:view name="curriculum" property="programI18N">
							<fr:layout>
								<fr:property name="escaped" value="false" />
								<fr:property name="newlineAware" value="false" />
							</fr:layout>
						</fr:view>
						</div>
					</logic:present>
					<logic:notPresent name="curriculum">
						<bean:message key="message.program.not.defined"/>
					</logic:notPresent>
		</logic:notEqual>
	</logic:iterate>