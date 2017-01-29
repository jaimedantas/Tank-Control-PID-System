
/*
 * QuanserClientException.java
 *
 * Created on 27 de Novembro de 2007, 22:38
 */

package sistemacontrole;

/**
 * Classe responsável pela recepção das excessões da Classe QuanserClient
 * @author Leonardo Dantas <lodantas@gmail.com>
 * @see QuanserClient
 */
public class QuanserClientException extends Exception {

	public QuanserClientException() {
	}

	public QuanserClientException(String arg0) {
		super(arg0);
	}

	public QuanserClientException(Throwable arg0) {
		super(arg0);
	}

	public QuanserClientException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
 

