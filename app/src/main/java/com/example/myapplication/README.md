# Project Structure

```
com.example.myapplication/
├── api/                    # REST API related classes
│   ├── ApiClient.java     # Retrofit client configuration
│   ├── ApiInterface.java  # API endpoints interface
│   └── ApiResponse.java   # Response wrapper classes
│
├── models/                # Model classes
│   └── [Model classes]    # Data model classes
│
├── controllers/           # Controller classes
│   └── [Controller classes] # Business logic classes
│
├── views/                 # View related classes
│   ├── activities/        # Activity classes
│   ├── fragments/         # Fragment classes
│   ├── adapters/          # RecyclerView adapters
│   └── customviews/       # Custom view components
│
├── utils/                 # Utility classes
│   ├── Constants.java     # Constants
│   ├── PrefManager.java   # SharedPreferences manager
│   └── [Other utilities]  # Other utility classes
│
└── di/                    # Dependency Injection
    └── AppModule.java     # Dagger/Hilt modules
```

## Package Descriptions

- **api**: Contains all REST API related classes including API client configuration, interfaces, and response models
- **models**: Contains data model classes that represent the data structure
- **controllers**: Contains business logic classes that handle data processing and business rules
- **views**: Contains all UI related classes including activities, fragments, adapters, and custom views
- **utils**: Contains utility classes and helper functions
- **di**: Contains dependency injection related classes 