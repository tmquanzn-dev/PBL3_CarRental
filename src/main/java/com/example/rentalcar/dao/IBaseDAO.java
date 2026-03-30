package com.example.rentalcar.dao;

import java.util.List;

/**
 * Interface nền tảng áp dụng Generics cho toàn bộ hệ thống DAO.
 * @param <T> Kiểu dữ liệu của Thực thể (Models - ví dụ: Users, Vehicles)
 * @param <K> Kiểu dữ liệu của Khóa chính (Primary Key - ví dụ: Integer, String)
 */
public interface IBaseDAO<T, K>
{

    boolean insert(T entity);

    boolean update(T entity);

    // Áp dụng Soft Delete (ẩn đi chứ không xóa thật) ở phần triển khai
    boolean delete(K id);

    T findById(K id);

    List<T> findAll();
}