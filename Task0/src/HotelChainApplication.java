package task0;

public class HotelChainApplication {
	
	public static void main(String[] args) {
		Terminal cli = new Terminal();
		
		cli.login();
		
		while (cli.getLogin())
			cli.waitCommand();
	}
}
