package com.fortagym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "detalle_rutina")
public class DetalleRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Size(max = 100, message = "El nombre del ejercicio no debe superar 100 caracteres")
    private String ejercicio;

    @NotBlank(message = "Las series y repeticiones son obligatorias")
    @Pattern(regexp = "^[0-9]{1,2}x[0-9]{1,3}$", 
             message = "Formato inválido. Ejemplo válido: 4x12")
    private String seriesReps;

    @NotBlank(message = "El descanso es obligatorio")
    @Pattern(regexp = "^[0-9]{1,3} ?(seg|s|min)$",
             message = "Formato inválido. Ejemplos: 60s, 90 seg, 2 min")
    private String descanso;

    @NotBlank(message = "Debe especificar los días")
    @Size(min = 3, max = 50, message = "Debe tener entre 3 y 50 caracteres")
    private String dias;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", nullable = false)
    @JsonIgnore // Evita que se serialice la rutina para prevenir ciclos infinitos en JSON
    private Rutina rutina;
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEjercicio() { return ejercicio; }
    public void setEjercicio(String ejercicio) { this.ejercicio = ejercicio; }

    public String getSeriesReps() { return seriesReps; }
    public void setSeriesReps(String seriesReps) { this.seriesReps = seriesReps; }

    public String getDescanso() { return descanso; }
    public void setDescanso(String descanso) { this.descanso = descanso; }

    public String getDias() { return dias; }
    public void setDias(String dias) { this.dias = dias; }

    public Rutina getRutina() { return rutina; }
    public void setRutina(Rutina rutina) { this.rutina = rutina; }
}
