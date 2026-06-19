package com.fullstack.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsersApplicationTests {

	@Test
	void applicationClass_isAvailableWithoutStartingSpringContext() {
		// Arrange
		Class<?> applicationClass = UsersApplication.class;

		// Act
		String simpleName = applicationClass.getSimpleName();

		// Assert
		assertEquals("UsersApplication", simpleName);
	}

}
