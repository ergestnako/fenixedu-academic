/*
 * Created on 11/Set/2003
 */
package ServidorAplicacao.Servico.manager;

import DataBeans.InfoExecutionCourse;
import DataBeans.util.Cloner;
import Dominio.DisciplinaExecucao;
import Dominio.IDisciplinaExecucao;
import ServidorAplicacao.IServico;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorAplicacao.Servico.exceptions.NonExistingServiceException;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;

/**
 * @author lmac1
 */

public class ReadExecutionCourse implements IServico {

	private static ReadExecutionCourse service = new ReadExecutionCourse();

	/**
	* The singleton access method of this class.
	*/
	public static ReadExecutionCourse getService() {
		return service;
	}

	/**
	* The constructor of this class.
	*/
	private ReadExecutionCourse() { }

	/**
	* Service name
	*/
	public final String getNome() {
		return "ReadExecutionCourse";
	}

	/**
	* Executes the service. Returns the current InfoExecutionCourse.
	*/
	public InfoExecutionCourse run(Integer idInternal) throws FenixServiceException {
		
		IDisciplinaExecucao executionCourse = null;
		
		try {
				ISuportePersistente sp = SuportePersistenteOJB.getInstance();
				executionCourse = (IDisciplinaExecucao) sp.getIDisciplinaExecucaoPersistente().readByOId(new DisciplinaExecucao(idInternal), false);
		} catch (ExcepcaoPersistencia excepcaoPersistencia){
			throw new FenixServiceException(excepcaoPersistencia);
		}
     
		if (executionCourse == null) {
			throw new NonExistingServiceException();
		}

		InfoExecutionCourse infoExecutionCourse = Cloner.copyIExecutionCourse2InfoExecutionCourse(executionCourse); 
		return infoExecutionCourse;
	}
}