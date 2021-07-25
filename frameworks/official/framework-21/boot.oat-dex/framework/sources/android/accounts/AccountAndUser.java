package android.accounts;

public class AccountAndUser {
    public Account account;
    public int userId;

    public AccountAndUser(Account account2, int userId2) {
        this.account = account2;
        this.userId = userId2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountAndUser)) {
            return false;
        }
        AccountAndUser other = (AccountAndUser) o;
        return this.account.equals(other.account) && this.userId == other.userId;
    }

    public int hashCode() {
        return this.account.hashCode() + this.userId;
    }

    public String toString() {
        return this.account.toString() + " u" + this.userId;
    }
}
