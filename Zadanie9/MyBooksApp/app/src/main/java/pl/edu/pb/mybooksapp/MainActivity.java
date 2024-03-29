package pl.edu.pb.mybooksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.book_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchBooksData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void fetchBooksData(String query){
        String finalQuery = prepareQuery(query);
        BookService bookService=RetrofitInstance.getRetrofitInstance().create(BookService.class);

        Call<BookContainer> booksApiCall=bookService.findBooks(finalQuery);

        booksApiCall.enqueue(new Callback<BookContainer>() {
            @Override
            public void onResponse(@NonNull Call<BookContainer> call, @NonNull Response<BookContainer> response) {
                if(response.body()!=null){
                    setupBookListView(response.body().getBookList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookContainer> call, @NonNull Throwable t) {
                Snackbar.make(findViewById(R.id.main_view),getString(R.string.api_error), BaseTransientBottomBar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private String prepareQuery(String query){
        String[] queryParts=query.split("\\s+");
        return TextUtils.join("+", queryParts);
    }

    private void setupBookListView(List<Book> books){
        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        final BookAdapter adapter=new BookAdapter();
        adapter.setBooks(books);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean checkNullOrEmpty(String text) {
        return text != null && !TextUtils.isEmpty(text);
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView bookTitleTextView;
        private TextView bookAuthorTextView;
        private TextView numberOfPagesTextView;
        private ImageView bookCover;
        private Book book;
        public BookHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.book_list_item, parent,false));

            bookTitleTextView=itemView.findViewById(R.id.book_title);
            bookAuthorTextView=itemView.findViewById(R.id.book_author);
            numberOfPagesTextView=itemView.findViewById(R.id.number_of_pages);
            bookCover = itemView.findViewById(R.id.img_cover);

            itemView.setOnClickListener(this);
        }
        public void bind(Book book){
            if(book != null && checkNullOrEmpty(book.getTitle()) && book.getAuthors() != null){
                this.book=book;
                bookTitleTextView.setText(book.getTitle());
                bookAuthorTextView.setText(TextUtils.join(", ", book.getAuthors()));
                numberOfPagesTextView.setText(book.getNumberOfPages());

                if(book.getCover()!=null){
                    Picasso.with(itemView.getContext())
                            .load(IMAGE_URL_BASE + book.getCover() + "-S.jpg")
                            .placeholder(R.drawable.book_icon)
                            .into(bookCover);
                }
                else{
                    bookCover.setImageResource(R.drawable.book_icon);
                }
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, BookDetailsActivity.class);
            intent.putExtra(BookDetailsActivity.BOOK_DETAILS_TITLE, book.getTitle());
            intent.putExtra(BookDetailsActivity.BOOK_DETAILS_AUTHOR, TextUtils.join(", ", book.getAuthors()));
            intent.putExtra(BookDetailsActivity.BOOK_DETAILS_COVER, book.getCover());
            intent.putExtra(BookDetailsActivity.BOOK_DETAILS_SUBTITLE, book.getSubtitle());
            intent.putExtra(BookDetailsActivity.BOOK_DETAILS_PUBLISHER, book.getPublisher());
            intent.putExtra(BookDetailsActivity.BOOK_DETAILS_PAGES, book.getNumberOfPages());
            startActivity(intent);
        }
    }
    private class BookAdapter extends RecyclerView.Adapter<BookHolder> {
        private List<Book> books;

        @NonNull
        @Override
        public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            return new BookHolder(getLayoutInflater(),parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BookHolder holder, int position){
            if(books!=null){
                Book book=books.get(position);
                holder.bind(book);
            } else {
                Log.d("MainActivity","No books");
            }
        }

        @Override
        public int getItemCount(){
            if(books!=null) return books.size(); else return 0;
        }

        void setBooks(List<Book> books){
            this.books=books;
            notifyDataSetChanged();
        }
    }
}