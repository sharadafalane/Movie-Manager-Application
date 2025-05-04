package com.movie.movieManager.service;

import com.movie.movieManager.dto.MovieDto;
import com.movie.movieManager.dto.MoviePageResponse;
import com.movie.movieManager.model.Movie;
import com.movie.movieManager.exception.FileExistsException;
import com.movie.movieManager.exception.MovieNotFoundException;
import com.movie.movieManager.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
public class MovieServiceImpl implements MovieService {

  private final MovieRepository movieRepository;

  private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // To upload file
        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new FileExistsException("File already exists! Please enter another file");
        }
        String uploadFileName = fileService.uploadFile(path, file);

        // Set the value of field 'poster' as filename
        movieDto.setPoster(uploadFileName);

        // Map dto to movie object
        Movie movie = new Movie(
                                null,
                                movieDto.getTitle(),
                                movieDto.getDirector(),
                                movieDto.getStudio(),
                                movieDto.getMovieCast(),
                                movieDto.getReleaseYear(),
                                movieDto.getPoster());

        // To save movie object & return save movie object
        Movie savedMovie = movieRepository.save(movie);

        //Generate poster Url
        String posterUrl = baseUrl + "/file/" + uploadFileName;

        // Generate poster url and map movie object and return it
        MovieDto response = new MovieDto(savedMovie.getMovieId(),
                                         savedMovie.getTitle(),
                                         savedMovie.getDirector(),
                                         savedMovie.getStudio(),
                                         savedMovie.getMovieCast(),
                                         savedMovie.getReleaseYear(),
                                         savedMovie.getPoster(),
                                         posterUrl);

        return response;

    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        // Check the data db and if exist , fetch data of given id
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id :"+ movieId));

        // Generate posterUrl
        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        // Map to movieDto objectand return it
        MovieDto response = new MovieDto(movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl);

        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // Fetch all data from DB
       List<Movie> movies = movieRepository.findAll();

       List<MovieDto> movieDtos = new ArrayList<>();

        //Iterate through the list, generate posterUrl for each movie obj and map to movieDto obj
        for(Movie movie: movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl);
            movieDtos.add(movieDto);
        }
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        // Check if movie object exists with given movie Id
        Movie mv = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id :"+ movieId));

        // If file is null, do nothing

        // If file is not null, then delete existing file associated with the record, and upload new file
        String fileName = mv.getPoster();
        if(file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        // Set movieDto's poster value, according to step2
        movieDto.setPoster(fileName);

        // Map it to movie obj
        Movie movie = new Movie(
                movieDto.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster());

        // Save the movie obj -> return saved obj
        Movie updatedMovie = movieRepository.save(movie);

        // Generate poster url
        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        // Map to movieDto and return it
        MovieDto response = new MovieDto(updatedMovie.getMovieId(),
                updatedMovie.getTitle(),
                updatedMovie.getDirector(),
                updatedMovie.getStudio(),
                updatedMovie.getMovieCast(),
                updatedMovie.getReleaseYear(),
                updatedMovie.getPoster(),
                posterUrl);

        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        // Check if movie obj exists
        Movie mv = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id :"+ movieId));
        Integer id = mv.getMovieId();

        // Delete the file associated with this object
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));

        // Delete the movie
        movieRepository.delete(mv);

        return "Movie deleted with id :"+  id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviePages = movieRepository.findAll(pageable);

        List<Movie> movies = moviePages.getContent();
        List<MovieDto> movieDtos = new ArrayList<>();
        for(Movie movie: movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl);
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                                    moviePages.getTotalElements(),
                                    moviePages.getTotalPages(),
                                      moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {
        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePages = movieRepository.findAll(pageable);

        List<Movie> movies = moviePages.getContent();
        List<MovieDto> movieDtos = new ArrayList<>();
        for(Movie movie: movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl);
            movieDtos.add(movieDto);
        }

        return  new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }


}
