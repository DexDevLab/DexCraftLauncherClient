package net.dex.dexcraft.launcher.tools;


/**
 *
 */
public class Account
{

  /**
   * Validate login according to informed user and password.
   * @param user the username
   * @param password the account password
   * @return the code according to the data informed, as follows:<br>
   * 0 - the fields are ok<br>
   * 1 - username is empty<br>
   * 2 - password is empty<br>
   * 3 - password doesn't match the length required or contain spaces<br>
   * 4 - password contain letters or symbols<br>
   * 5 - user doesn't match the length required or contain spaces<br>
   * 6 - user contain special characters
   */
  private int validateFields(String user, String password)
  {
    if (user.isEmpty())
    {
      return 1;
    }
    else if (password.isEmpty())
    {
      return 2;
    }
    else if ( (password.length() < 4) || (password.length() > 8) || (password.contains(" ")) )
    {
     return 3;
    }
    else if (!password.matches("[0-9]+"))
    {
      return 4;
    }
    else if ( (user.length() < 3) || (user.length() > 12) || (user.contains(" ")) )
    {
      return 5;
    }
    else if (!user.matches("[a-zA-Z0-9]+"))
    {
      return 6;
    }
    return 0;
  }

  public Integer login(String user, String password)
  {
    int loginCode = validateFields(user, password);
    if (loginCode == 0)
    {





    }
    return loginCode;
  }



}
