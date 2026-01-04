/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */

public interface Approvable {
    boolean approve(Admin admin);
    boolean reject(Admin admin);
    String getStatus();
}
