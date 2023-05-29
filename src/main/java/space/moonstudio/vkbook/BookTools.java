package space.moonstudio.vkbook;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookTools {
    private final VKBook pl;
    private final ItemStack book;
    public BookTools(String title, String author, VKBook pl) {
        this.pl = pl;

        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        bookMeta.setAuthor(author);
        bookMeta.setTitle(title);

        book.setItemMeta(bookMeta);
    }

    public void addText(String text) {
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        List<String> strings = pl.getUtils().splitString(text, 256);

        for(String page : strings) {
            bookMeta.addPage(page);
        }

        book.setItemMeta(bookMeta);
    }

    public void showBook(Player p) {
        p.openBook(book);
    }
}
